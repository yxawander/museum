package com.museum.booking.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.museum.booking.common.BusinessException;
import com.museum.booking.common.PageResult;
import com.museum.booking.dto.ReservationDtos;
import com.museum.booking.entity.Exhibition;
import com.museum.booking.entity.Reservation;
import com.museum.booking.entity.SysUser;
import com.museum.booking.entity.VisitSlot;
import com.museum.booking.mapper.ExhibitionMapper;
import com.museum.booking.mapper.ReservationMapper;
import com.museum.booking.mapper.SysUserMapper;
import com.museum.booking.mapper.VisitSlotMapper;
import com.museum.booking.security.LoginUser;
import com.museum.booking.vo.ReservationVO;
import com.museum.booking.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 预约核心业务：下单、取消、核销、查询与导出。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {

    private static final int MAX_TICKETS = 5;
    private static final int BOOKING_CUTOFF_MINUTES = 30;

    private final ReservationMapper reservationMapper;
    private final VisitSlotMapper slotMapper;
    private final ExhibitionMapper exhibitionMapper;
    private final SysUserMapper userMapper;

    @Transactional
    public ReservationVO create(Long userId, ReservationDtos.CreateRequest request) {
        VisitSlot slot = slotMapper.selectById(request.slotId());
        if (slot == null) {
            throw new BusinessException(404, "参观时段不存在");
        }
        if (!"OPEN".equals(slot.getStatus())) {
            throw new BusinessException(409, "该时段已关闭预约");
        }

        LocalDateTime slotStart = LocalDateTime.of(slot.getVisitDate(), slot.getStartTime());
        if (!slotStart.isAfter(LocalDateTime.now().plusMinutes(BOOKING_CUTOFF_MINUTES))) {
            throw new BusinessException(409, "该时段已停止预约，请选择其他时段");
        }

        Exhibition exhibition = exhibitionMapper.selectById(slot.getExhibitionId());
        if (exhibition == null || !"PUBLISHED".equals(exhibition.getStatus())) {
            throw new BusinessException(409, "对应展览已下架");
        }
        if (slot.getVisitDate().isBefore(exhibition.getStartDate()) || slot.getVisitDate().isAfter(exhibition.getEndDate())) {
            throw new BusinessException(409, "预约日期不在展览展期内");
        }

        long duplicated = reservationMapper.selectCount(new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getUserId, userId)
                .eq(Reservation::getSlotId, slot.getId())
                .in(Reservation::getStatus, "CONFIRMED", "CHECKED_IN"));
        if (duplicated > 0) {
            throw new BusinessException(409, "您已预约过该时段，请勿重复预约");
        }

        int updated = slotMapper.reserveCapacity(slot.getId(), request.ticketCount());
        if (updated == 0) {
            throw new BusinessException(409, "该时段余票不足，请选择其他时段");
        }

        Reservation reservation = new Reservation();
        reservation.setOrderNo(generateOrderNo());
        reservation.setUserId(userId);
        reservation.setSlotId(slot.getId());
        reservation.setExhibitionId(slot.getExhibitionId());
        reservation.setVisitDate(slot.getVisitDate());
        reservation.setStartTime(slot.getStartTime());
        reservation.setEndTime(slot.getEndTime());
        reservation.setVisitorName(request.visitorName().trim());
        reservation.setVisitorIdCard(request.visitorIdCard().toUpperCase());
        reservation.setVisitorPhone(request.visitorPhone().trim());
        reservation.setTicketCount(request.ticketCount());
        reservation.setStatus("CONFIRMED");
        reservation.setTicketCode(generateTicketCode());
        reservation.setRemark(request.remark());
        reservation.setSource("WEB");
        reservationMapper.insert(reservation);

        return toVOList(List.of(reservation)).getFirst();
    }

    public PageResult<ReservationVO> myPage(Long userId, int page, int size, String status) {
        LambdaQueryWrapper<Reservation> wrapper = new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getUserId, userId);
        if (StringUtils.hasText(status)) {
            wrapper.eq(Reservation::getStatus, status);
        }
        wrapper.orderByDesc(Reservation::getCreatedAt);
        Page<Reservation> result = reservationMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResult.of(toVOList(result.getRecords()), result.getTotal(), result.getCurrent(), result.getSize());
    }

    public PageResult<ReservationVO> adminPage(int page, int size, String keyword, String status,
                                               LocalDate visitDate, Long exhibitionId) {
        Page<Reservation> result = reservationMapper.selectPage(new Page<>(page, size),
                adminWrapper(keyword, status, visitDate, exhibitionId));
        return PageResult.of(toVOList(result.getRecords()), result.getTotal(), result.getCurrent(), result.getSize());
    }

    public ReservationVO detail(Long id, LoginUser loginUser) {
        Reservation reservation = requireReservation(id);
        if (!loginUser.isAdmin() && !loginUser.isStaff() && !reservation.getUserId().equals(loginUser.id())) {
            throw new BusinessException(403, "无权查看该预约");
        }
        return toVOList(List.of(reservation)).getFirst();
    }

    @Transactional
    public void cancel(Long userId, Long id, String reason, boolean admin) {
        Reservation reservation = requireReservation(id);
        if (!admin && !reservation.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权取消该预约");
        }
        if (!"CONFIRMED".equals(reservation.getStatus())) {
            throw new BusinessException(400, "当前状态不可取消");
        }
        LocalDateTime slotStart = LocalDateTime.of(reservation.getVisitDate(), reservation.getStartTime());
        if (!admin && !LocalDateTime.now().isBefore(slotStart)) {
            throw new BusinessException(400, "参观已开始，无法取消预约");
        }

        int updated = reservationMapper.update(null, new LambdaUpdateWrapper<Reservation>()
                .eq(Reservation::getId, id)
                .eq(Reservation::getStatus, "CONFIRMED")
                .set(Reservation::getStatus, "CANCELLED")
                .set(Reservation::getCancelReason, StringUtils.hasText(reason) ? reason.trim() : "用户取消")
                .set(Reservation::getCancelledAt, LocalDateTime.now()));
        if (updated == 0) {
            throw new BusinessException(409, "预约状态已变化，请刷新后重试");
        }
        slotMapper.releaseCapacity(reservation.getSlotId(), reservation.getTicketCount());
    }

    public ReservationVO preview(String code) {
        return toVOList(List.of(findByCode(code))).getFirst();
    }

    @Transactional
    public ReservationVO checkin(String code, LoginUser operator) {
        Reservation reservation = findByCode(code);
        if (!"CONFIRMED".equals(reservation.getStatus())) {
            throw new BusinessException(409, "该预约当前状态为“" + ReservationVO.statusText(reservation.getStatus()) + "”，不能核销");
        }
        LocalDate today = LocalDate.now();
        if (!today.equals(reservation.getVisitDate())) {
            throw new BusinessException(409, "该预约的参观日期不是今天，不能核销");
        }
        if (LocalTime.now().isAfter(reservation.getEndTime())) {
            throw new BusinessException(409, "该时段已结束，不能核销");
        }

        int updated = reservationMapper.update(null, new LambdaUpdateWrapper<Reservation>()
                .eq(Reservation::getId, reservation.getId())
                .eq(Reservation::getStatus, "CONFIRMED")
                .set(Reservation::getStatus, "CHECKED_IN")
                .set(Reservation::getCheckinAt, LocalDateTime.now()));
        if (updated == 0) {
            throw new BusinessException(409, "核销失败，预约状态已变化");
        }
        reservation.setStatus("CHECKED_IN");
        reservation.setCheckinAt(LocalDateTime.now());
        log.info("预约核销成功 orderNo={} operator={}", reservation.getOrderNo(), operator.username());
        return toVOList(List.of(reservation)).getFirst();
    }

    /**
     * 将已过期的“待参观”订单标记为已过期，由定时任务调用。
     */
    public int expirePastReservations() {
        return reservationMapper.expirePastReservations(LocalDate.now(), LocalTime.now());
    }

    public String exportCsv(String keyword, String status, LocalDate visitDate, Long exhibitionId) {
        List<Reservation> reservations = reservationMapper.selectList(
                adminWrapper(keyword, status, visitDate, exhibitionId).last("LIMIT 5000"));
        List<ReservationVO> list = toVOList(reservations);
        StringBuilder sb = new StringBuilder();
        sb.append("预约单号,展览名称,参观日期,时段,参观人,身份证号,手机号,人数,状态,预约时间,核销时间\n");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (ReservationVO vo : list) {
            sb.append(csv(vo.getOrderNo())).append(',')
                    .append(csv(vo.getExhibitionTitle())).append(',')
                    .append(vo.getVisitDate()).append(',')
                    .append(vo.getStartTime()).append('-').append(vo.getEndTime()).append(',')
                    .append(csv(vo.getVisitorName())).append(',')
                    .append(csv(vo.getVisitorIdCard())).append(',')
                    .append(csv(vo.getVisitorPhone())).append(',')
                    .append(vo.getTicketCount()).append(',')
                    .append(csv(vo.getStatusText())).append(',')
                    .append(vo.getCreatedAt() == null ? "" : dtf.format(vo.getCreatedAt())).append(',')
                    .append(vo.getCheckinAt() == null ? "" : dtf.format(vo.getCheckinAt())).append('\n');
        }
        return sb.toString();
    }

    public List<ReservationVO> toVOList(List<Reservation> reservations) {
        if (reservations == null || reservations.isEmpty()) {
            return List.of();
        }
        List<Long> exhibitionIds = reservations.stream().map(Reservation::getExhibitionId).distinct().toList();
        Map<Long, Exhibition> exhibitions = exhibitionMapper.selectBatchIds(exhibitionIds).stream()
                .collect(Collectors.toMap(Exhibition::getId, Function.identity(), (a, b) -> a));
        List<Long> userIds = reservations.stream().map(Reservation::getUserId).distinct().toList();
        Map<Long, SysUser> users = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, Function.identity(), (a, b) -> a));

        List<ReservationVO> result = new ArrayList<>(reservations.size());
        for (Reservation r : reservations) {
            Exhibition exhibition = exhibitions.get(r.getExhibitionId());
            SysUser user = users.get(r.getUserId());
            LocalDateTime start = LocalDateTime.of(r.getVisitDate(), r.getStartTime());
            boolean canCancel = "CONFIRMED".equals(r.getStatus()) && LocalDateTime.now().isBefore(start);
            result.add(ReservationVO.builder()
                    .id(r.getId())
                    .orderNo(r.getOrderNo())
                    .userId(r.getUserId())
                    .username(user == null ? null : user.getUsername())
                    .slotId(r.getSlotId())
                    .exhibitionId(r.getExhibitionId())
                    .exhibitionTitle(exhibition == null ? "展览已删除" : exhibition.getTitle())
                    .coverUrl(exhibition == null ? null : exhibition.getCoverUrl())
                    .venue(exhibition == null ? null : exhibition.getVenue())
                    .visitDate(r.getVisitDate())
                    .startTime(r.getStartTime())
                    .endTime(r.getEndTime())
                    .visitorName(r.getVisitorName())
                    .visitorIdCard(UserVO.maskIdCard(r.getVisitorIdCard()))
                    .visitorPhone(r.getVisitorPhone())
                    .ticketCount(r.getTicketCount())
                    .status(r.getStatus())
                    .statusText(ReservationVO.statusText(r.getStatus()))
                    .ticketCode(r.getTicketCode())
                    .qrContent("HBMUSEUM:" + r.getOrderNo() + ":" + r.getTicketCode())
                    .remark(r.getRemark())
                    .cancelReason(r.getCancelReason())
                    .cancelledAt(r.getCancelledAt())
                    .checkinAt(r.getCheckinAt())
                    .createdAt(r.getCreatedAt())
                    .canCancel(canCancel)
                    .build());
        }
        return result;
    }

    private LambdaQueryWrapper<Reservation> adminWrapper(String keyword, String status, LocalDate visitDate,
                                                         Long exhibitionId) {
        LambdaQueryWrapper<Reservation> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(Reservation::getOrderNo, kw)
                    .or().like(Reservation::getVisitorName, kw)
                    .or().like(Reservation::getVisitorPhone, kw)
                    .or().like(Reservation::getTicketCode, kw));
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(Reservation::getStatus, status);
        }
        if (visitDate != null) {
            wrapper.eq(Reservation::getVisitDate, visitDate);
        }
        if (exhibitionId != null) {
            wrapper.eq(Reservation::getExhibitionId, exhibitionId);
        }
        wrapper.orderByDesc(Reservation::getCreatedAt);
        return wrapper;
    }

    private Reservation requireReservation(Long id) {
        Reservation reservation = reservationMapper.selectById(id);
        if (reservation == null) {
            throw new BusinessException(404, "预约记录不存在");
        }
        return reservation;
    }

    private Reservation findByCode(String code) {
        String value = code == null ? "" : code.trim();
        if (value.isEmpty()) {
            throw new BusinessException(400, "请输入预约单号或核销码");
        }
        Reservation reservation = reservationMapper.selectOne(new LambdaQueryWrapper<Reservation>()
                .and(w -> w.eq(Reservation::getOrderNo, value).or().eq(Reservation::getTicketCode, value))
                .last("LIMIT 1"));
        if (reservation == null) {
            throw new BusinessException(404, "未找到对应的预约记录");
        }
        return reservation;
    }

    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = ThreadLocalRandom.current().nextInt(100000, 1000000);
        return "HB" + timestamp + random;
    }

    private String generateTicketCode() {
        return "TC" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 20).toUpperCase();
    }

    private String csv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}