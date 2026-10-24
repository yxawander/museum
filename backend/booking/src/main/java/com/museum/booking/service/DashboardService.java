package com.museum.booking.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.museum.booking.entity.Exhibition;
import com.museum.booking.entity.Feedback;
import com.museum.booking.entity.Reservation;
import com.museum.booking.entity.SysUser;
import com.museum.booking.mapper.FeedbackMapper;
import com.museum.booking.mapper.ReservationMapper;
import com.museum.booking.mapper.StatsMapper;
import com.museum.booking.mapper.SysUserMapper;
import com.museum.booking.mapper.ExhibitionMapper;
import com.museum.booking.mapper.VisitSlotMapper;
import com.museum.booking.vo.DashboardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 后台数据看板服务。
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final SysUserMapper userMapper;
    private final ExhibitionMapper exhibitionMapper;
    private final ReservationMapper reservationMapper;
    private final VisitSlotMapper slotMapper;
    private final FeedbackMapper feedbackMapper;
    private final StatsMapper statsMapper;
    private final ReservationService reservationService;

    public DashboardVO overview() {
        LocalDate today = LocalDate.now();

        long visitorCount = userMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getRole, "VISITOR"));
        long exhibitionCount = exhibitionMapper.selectCount(new LambdaQueryWrapper<Exhibition>()
                .eq(Exhibition::getStatus, "PUBLISHED"));
        long todayReservationCount = reservationMapper.selectCount(new LambdaQueryWrapper<Reservation>()
                .ge(Reservation::getCreatedAt, today.atStartOfDay()));
        long todayVisitorCount = reservationMapper.sumVisitorsByDate(today);
        long todayCheckedCount = reservationMapper.sumCheckedVisitorsByDate(today);
        long pendingFeedbackCount = feedbackMapper.selectCount(new LambdaQueryWrapper<Feedback>()
                .eq(Feedback::getStatus, "PENDING"));
        long totalReservationCount = reservationMapper.selectCount(null);

        Map<String, Object> capacityMap = slotMapper.sumCapacityByDate(today);
        long todayCapacity = toLong(capacityMap.get("capacity"));
        long todayBooked = toLong(capacityMap.get("booked"));

        double checkinRate = todayVisitorCount == 0 ? 0
                : Math.round(todayCheckedCount * 1000.0 / todayVisitorCount) / 10.0;

        List<DashboardVO.TrendPoint> trend = buildTrend();
        List<DashboardVO.NameValue> categories = statsMapper.selectCategoryDistribution().stream()
                .map(m -> new DashboardVO.NameValue(String.valueOf(m.get("name")), toLong(m.get("value"))))
                .toList();
        List<DashboardVO.NameValue> topExhibitions = reservationMapper.selectTopExhibitions().stream()
                .map(m -> new DashboardVO.NameValue(String.valueOf(m.get("name")), toLong(m.get("value"))))
                .toList();

        Page<Reservation> recentPage = reservationMapper.selectPage(new Page<>(1, 8),
                new LambdaQueryWrapper<Reservation>().orderByDesc(Reservation::getCreatedAt));

        return DashboardVO.builder()
                .visitorCount(visitorCount)
                .exhibitionCount(exhibitionCount)
                .todayReservationCount(todayReservationCount)
                .todayVisitorCount(todayVisitorCount)
                .todayCheckedCount(todayCheckedCount)
                .pendingFeedbackCount(pendingFeedbackCount)
                .totalReservationCount(totalReservationCount)
                .todayCapacity(todayCapacity)
                .todayBooked(todayBooked)
                .checkinRate(checkinRate)
                .trend(trend)
                .categoryDistribution(categories)
                .topExhibitions(topExhibitions)
                .recentReservations(reservationService.toVOList(recentPage.getRecords()))
                .build();
    }

    private List<DashboardVO.TrendPoint> buildTrend() {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(6);
        List<Map<String, Object>> rows = reservationMapper.selectVisitTrend(start, end);
        Map<String, Map<String, Object>> byDay = new HashMap<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");
        for (Map<String, Object> row : rows) {
            byDay.put(String.valueOf(row.get("stat_day")), row);
        }
        List<DashboardVO.TrendPoint> result = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = start.plusDays(i);
            String key = date.format(fmt);
            Map<String, Object> row = byDay.get(key);
            result.add(new DashboardVO.TrendPoint(key,
                    row == null ? 0 : toLong(row.get("total")),
                    row == null ? 0 : toLong(row.get("checked"))));
        }
        return result;
    }

    private long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    /**
     * 提供给首页使用的轻量统计。
     */
    public Map<String, Object> publicStats() {
        LocalDate today = LocalDate.now();
        Map<String, Object> map = new HashMap<>();
        map.put("exhibitionCount", exhibitionMapper.selectCount(new LambdaQueryWrapper<Exhibition>()
                .eq(Exhibition::getStatus, "PUBLISHED")));
        map.put("todayVisitorCount", reservationMapper.sumVisitorsByDate(today));
        map.put("totalReservationCount", reservationMapper.selectCount(null));
        Map<String, Object> capacityMap = slotMapper.sumCapacityByDate(today);
        long capacity = toLong(capacityMap.get("capacity"));
        long booked = toLong(capacityMap.get("booked"));
        map.put("todayRemaining", Math.max(0, capacity - booked));
        map.put("todayCapacity", capacity);
        map.put("openTime", "09:00");
        map.put("closeTime", "17:00");
        return map;
    }
}