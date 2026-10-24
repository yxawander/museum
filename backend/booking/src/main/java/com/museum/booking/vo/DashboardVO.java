package com.museum.booking.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 后台数据看板。
 */
@Data
@Builder
public class DashboardVO {

    private long visitorCount;
    private long exhibitionCount;
    private long todayReservationCount;
    private long todayVisitorCount;
    private long todayCheckedCount;
    private long pendingFeedbackCount;
    private long totalReservationCount;
    private long todayCapacity;
    private long todayBooked;
    private double checkinRate;
    private List<TrendPoint> trend;
    private List<NameValue> categoryDistribution;
    private List<NameValue> topExhibitions;
    private List<ReservationVO> recentReservations;

    public record TrendPoint(String day, long total, long checked) {
    }

    public record NameValue(String name, long value) {
    }
}