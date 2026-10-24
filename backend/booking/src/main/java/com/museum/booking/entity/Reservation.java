package com.museum.booking.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 预约订单。
 */
@Data
@TableName("reservation")
public class Reservation {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;
    private Long userId;
    private Long slotId;
    private Long exhibitionId;
    private LocalDate visitDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String visitorName;
    private String visitorIdCard;
    private String visitorPhone;
    private Integer ticketCount;

    /** CONFIRMED / CHECKED_IN / CANCELLED / EXPIRED */
    private String status;

    private String ticketCode;
    private String remark;
    private String cancelReason;
    private LocalDateTime cancelledAt;
    private LocalDateTime checkinAt;
    private String source;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}