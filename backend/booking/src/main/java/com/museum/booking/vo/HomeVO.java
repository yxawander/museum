package com.museum.booking.vo;

import com.museum.booking.entity.Announcement;
import com.museum.booking.entity.Exhibition;

import java.util.List;
import java.util.Map;

/**
 * 首页聚合数据。
 */
public record HomeVO(
        List<Exhibition> exhibitions,
        List<Announcement> announcements,
        Map<String, Object> stats) {
}