package com.museum.booking.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 后台统计查询。
 */
@Mapper
public interface StatsMapper {

    @Select("""
            SELECT category AS name, COUNT(*) AS value
            FROM exhibition
            WHERE status = 'PUBLISHED'
            GROUP BY category
            """)
    List<Map<String, Object>> selectCategoryDistribution();

    @Select("""
            SELECT DATE_FORMAT(visit_date, '%m-%d') AS stat_day,
                   SUM(ticket_count) AS total,
                   SUM(CASE WHEN status = 'CHECKED_IN' THEN ticket_count ELSE 0 END) AS checked
            FROM reservation
            WHERE visit_date >= DATE_SUB(CURDATE(), INTERVAL 6 DAY)
              AND visit_date <= CURDATE()
              AND status IN ('CHECKED_IN', 'EXPIRED', 'CONFIRMED')
            GROUP BY visit_date
            ORDER BY visit_date
            """)
    List<Map<String, Object>> selectRecentVisitStats();
}