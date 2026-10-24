package com.museum.booking.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.museum.booking.common.BusinessException;
import com.museum.booking.common.PageResult;
import com.museum.booking.dto.AdminDtos;
import com.museum.booking.entity.Exhibition;
import com.museum.booking.entity.Reservation;
import com.museum.booking.entity.VisitSlot;
import com.museum.booking.mapper.ExhibitionMapper;
import com.museum.booking.mapper.ReservationMapper;
import com.museum.booking.mapper.VisitSlotMapper;
import com.museum.booking.vo.SlotVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 参观时段与库存服务。
 */
@Service
@RequiredArgsConstructor
public class SlotService {

    private static final Map<LocalTime, LocalTime> DEFAULT_ENDS = Map.of(
            LocalTime.of(9, 0), LocalTime.of(11, 0),
            LocalTime.of(11, 0), LocalTime.of(13, 0),
            LocalTime.of(13, 0), LocalTime.of(16, 0));

    private final VisitSlotMapper slotMapper;
    private final ExhibitionMapper exhibitionMapper;
    private final ReservationMapper reservationMapper;

    public List<SlotVO> publicList(Long exhibitionId, LocalDate startDate, LocalDate endDate) {
        if (exhibitionId == null) {
            throw new BusinessException(400, "请选择展览");
        }
        LocalDate today = LocalDate.now();
        LocalDate start = startDate == null ? today : startDate;
        LocalDate end = endDate == null ? start.plusDays(6) : endDate;
        if (start.isBefore(today)) {
            start = today;
        }
        if (end.isBefore(start)) {
            throw new BusinessException(400, "结束日期不能早于开始日期");
        }
        if (ChronoUnit.DAYS.between(start, end) > 31) {
            throw new BusinessException(400, "单次最多查询 31 天");
        }
        List<VisitSlot> slots = slotMapper.selectList(new LambdaQueryWrapper<VisitSlot>()
                .eq(VisitSlot::getExhibitionId, exhibitionId)
                .between(VisitSlot::getVisitDate, start, end)
                .orderByAsc(VisitSlot::getVisitDate)
                .orderByAsc(VisitSlot::getStartTime));
        if (slots.isEmpty()) {
            return List.of();
        }
        Exhibition exhibition = exhibitionMapper.selectById(exhibitionId);
        String title = exhibition == null ? null : exhibition.getTitle();
        return slots.stream().map(s -> SlotVO.from(s, title)).toList();
    }

    public PageResult<SlotVO> adminPage(int page, int size, Long exhibitionId, LocalDate date, String status) {
        LambdaQueryWrapper<VisitSlot> wrapper = new LambdaQueryWrapper<>();
        if (exhibitionId != null) {
            wrapper.eq(VisitSlot::getExhibitionId, exhibitionId);
        }
        if (date != null) {
            wrapper.eq(VisitSlot::getVisitDate, date);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(VisitSlot::getStatus, status);
        }
        wrapper.orderByDesc(VisitSlot::getVisitDate).orderByAsc(VisitSlot::getStartTime);
        Page<VisitSlot> result = slotMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResult.of(toVOList(result.getRecords()), result.getTotal(), result.getCurrent(), result.getSize());
    }

    public SlotVO create(AdminDtos.SlotForm form) {
        Exhibition exhibition = requireExhibition(form.exhibitionId());
        validateTime(form.startTime(), form.endTime());
        long exists = slotMapper.selectCount(new LambdaQueryWrapper<VisitSlot>()
                .eq(VisitSlot::getExhibitionId, form.exhibitionId())
                .eq(VisitSlot::getVisitDate, form.visitDate())
                .eq(VisitSlot::getStartTime, form.startTime()));
        if (exists > 0) {
            throw new BusinessException(409, "该展览当天已存在相同时段");
        }
        VisitSlot slot = new VisitSlot();
        applyForm(slot, form);
        slot.setBookedCount(0);
        if (!StringUtils.hasText(slot.getStatus())) {
            slot.setStatus("OPEN");
        }
        slotMapper.insert(slot);
        return SlotVO.from(slot, exhibition.getTitle());
    }

    public SlotVO update(Long id, AdminDtos.SlotForm form) {
        VisitSlot slot = requireSlot(id);
        Exhibition exhibition = requireExhibition(form.exhibitionId());
        validateTime(form.startTime(), form.endTime());
        if (form.capacity() < slot.getBookedCount()) {
            throw new BusinessException(409, "容量不能小于当前已预约人数 " + slot.getBookedCount());
        }
        long exists = slotMapper.selectCount(new LambdaQueryWrapper<VisitSlot>()
                .eq(VisitSlot::getExhibitionId, form.exhibitionId())
                .eq(VisitSlot::getVisitDate, form.visitDate())
                .eq(VisitSlot::getStartTime, form.startTime())
                .ne(VisitSlot::getId, id));
        if (exists > 0) {
            throw new BusinessException(409, "该展览当天已存在相同时段");
        }
        applyForm(slot, form);
        if (!StringUtils.hasText(slot.getStatus())) {
            slot.setStatus("OPEN");
        }
        slotMapper.updateById(slot);
        return SlotVO.from(slot, exhibition.getTitle());
    }

    public void updateStatus(Long id, String status) {
        if (!"OPEN".equals(status) && !"CLOSED".equals(status)) {
            throw new BusinessException(400, "非法的时段状态");
        }
        requireSlot(id);
        VisitSlot update = new VisitSlot();
        update.setId(id);
        update.setStatus(status);
        slotMapper.updateById(update);
    }

    @Transactional
    public int batchGenerate(AdminDtos.BatchSlotForm form) {
        requireExhibition(form.exhibitionId());
        if (form.endDate().isBefore(form.startDate())) {
            throw new BusinessException(400, "结束日期不能早于开始日期");
        }
        if (ChronoUnit.DAYS.between(form.startDate(), form.endDate()) > 31) {
            throw new BusinessException(400, "单次最多生成 31 天");
        }
        List<LocalTime> starts = new ArrayList<>();
        for (String value : form.startTimes()) {
            try {
                starts.add(LocalTime.parse(value));
            } catch (Exception e) {
                throw new BusinessException(400, "时段格式不正确：" + value);
            }
        }
        int created = 0;
        for (LocalDate date = form.startDate(); !date.isAfter(form.endDate()); date = date.plusDays(1)) {
            if (Boolean.TRUE.equals(form.skipMonday()) && date.getDayOfWeek() == DayOfWeek.MONDAY) {
                continue;
            }
            for (LocalTime start : starts) {
                LocalTime end = DEFAULT_ENDS.getOrDefault(start, start.plusHours(2));
                if (!end.isAfter(start)) {
                    continue;
                }
                long exists = slotMapper.selectCount(new LambdaQueryWrapper<VisitSlot>()
                        .eq(VisitSlot::getExhibitionId, form.exhibitionId())
                        .eq(VisitSlot::getVisitDate, date)
                        .eq(VisitSlot::getStartTime, start));
                if (exists > 0) {
                    continue;
                }
                VisitSlot slot = new VisitSlot();
                slot.setExhibitionId(form.exhibitionId());
                slot.setVisitDate(date);
                slot.setStartTime(start);
                slot.setEndTime(end);
                slot.setCapacity(form.capacity());
                slot.setBookedCount(0);
                slot.setStatus("OPEN");
                slot.setRemark("批量生成");
                slotMapper.insert(slot);
                created++;
            }
        }
        return created;
    }

    @Transactional
    public void delete(Long id) {
        VisitSlot slot = requireSlot(id);
        long reservations = reservationMapper.selectCount(new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getSlotId, id));
        if (reservations > 0) {
            throw new BusinessException(409, "该时段已有预约记录，不能删除，可将其关闭");
        }
        slotMapper.deleteById(id);
    }

    public List<SlotVO> toVOList(List<VisitSlot> slots) {
        if (slots == null || slots.isEmpty()) {
            return List.of();
        }
        List<Long> exhibitionIds = slots.stream().map(VisitSlot::getExhibitionId).distinct().toList();
        Map<Long, String> titles = exhibitionMapper.selectBatchIds(exhibitionIds).stream()
                .collect(Collectors.toMap(Exhibition::getId, Exhibition::getTitle, (a, b) -> a));
        return slots.stream().map(s -> SlotVO.from(s, titles.get(s.getExhibitionId()))).toList();
    }

    private void applyForm(VisitSlot slot, AdminDtos.SlotForm form) {
        slot.setExhibitionId(form.exhibitionId());
        slot.setVisitDate(form.visitDate());
        slot.setStartTime(form.startTime());
        slot.setEndTime(form.endTime());
        slot.setCapacity(form.capacity());
        slot.setStatus(form.status());
        slot.setRemark(form.remark());
    }

    private void validateTime(LocalTime start, LocalTime end) {
        if (!end.isAfter(start)) {
            throw new BusinessException(400, "结束时间必须晚于开始时间");
        }
    }

    private VisitSlot requireSlot(Long id) {
        VisitSlot slot = slotMapper.selectById(id);
        if (slot == null) {
            throw new BusinessException(404, "参观时段不存在");
        }
        return slot;
    }

    private Exhibition requireExhibition(Long id) {
        Exhibition exhibition = exhibitionMapper.selectById(id);
        if (exhibition == null) {
            throw new BusinessException(404, "展览不存在");
        }
        return exhibition;
    }
}