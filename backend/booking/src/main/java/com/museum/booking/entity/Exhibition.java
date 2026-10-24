package com.museum.booking.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 展览信息。
 */
@Data
@TableName("exhibition")
public class Exhibition {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;
    private String subtitle;
    private String category;
    private String coverUrl;
    private String description;
    private String venue;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime openTime;
    private LocalTime closeTime;
    private BigDecimal ticketPrice;
    private String tips;

    /** DRAFT / PUBLISHED / CLOSED */
    private String status;

    private Integer sortOrder;
    private Integer viewCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}