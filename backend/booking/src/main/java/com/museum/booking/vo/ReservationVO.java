package com.museum.booking.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 预约订单视图对象。
 */
@Data
@Builder
public class ReservationVO {

    private Long id;
    private String orderNo;
    private Long userId;
    private String username;
    private Long slotId;
    private Long exhibitionId;
    private String exhibitionTitle;
    private String coverUrl;
    private String venue;
    private LocalDate visitDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String visitorName;
    private String visitorIdCard;
    private String visitorPhone;
    private Integer ticketCount;
    private String status;
    private String statusText;
    private String ticketCode;
    private String qrContent;
    private String remark;
    private String cancelReason;
    private LocalDateTime cancelledAt;
    private LocalDateTime checkinAt;
    private LocalDateTime createdAt;
    private Boolean canCancel;

    public static String statusText(String status) {
        return switch (status == null ? "" : status) {
            case "CONFIRMED" -> "待参观";
            case "CHECKED_IN" -> "已入馆";
            case "CANCELLED" -> "已取消";
            case "EXPIRED" -> "已过期";
            default -> status;
        };
    }
}