package com.museum.booking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.museum.booking.entity.Reservation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface ReservationMapper extends BaseMapper<Reservation> {

    @Update("""
            UPDATE reservation
            SET status = 'EXPIRED', updated_at = NOW()
            WHERE status = 'CONFIRMED'
              AND (visit_date < #{today}
                   OR (visit_date = #{today} AND end_time < #{now}))
            """)
    int expirePastReservations(@Param("today") LocalDate today, @Param("now") java.time.LocalTime now);

    @Select("""
            SELECT DATE_FORMAT(created_at, '%m-%d') AS stat_day, COUNT(*) AS total
            FROM reservation
            WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL 6 DAY)
            GROUP BY DATE(created_at)
            ORDER BY DATE(created_at)
            """)
    List<Map<String, Object>> selectDailyTrend();

    @Select("""
            SELECT e.title AS name, COUNT(r.id) AS value
            FROM reservation r
            JOIN exhibition e ON e.id = r.exhibition_id
            WHERE r.status IN ('CONFIRMED', 'CHECKED_IN', 'EXPIRED')
            GROUP BY e.id, e.title
            ORDER BY value DESC
            LIMIT 5
            """)
    List<Map<String, Object>> selectTopExhibitions();

    @Select("""
            SELECT DATE_FORMAT(visit_date, '%m-%d') AS stat_day,
                   SUM(ticket_count) AS total,
                   SUM(CASE WHEN status = 'CHECKED_IN' THEN ticket_count ELSE 0 END) AS checked
            FROM reservation
            WHERE visit_date BETWEEN #{start} AND #{end}
              AND status IN ('CONFIRMED', 'CHECKED_IN', 'EXPIRED')
            GROUP BY visit_date
            ORDER BY visit_date
            """)
    List<Map<String, Object>> selectVisitTrend(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Select("""
            SELECT COUNT(*) FROM reservation
            WHERE visit_date = #{date} AND status IN ('CONFIRMED', 'CHECKED_IN')
            """)
    long countVisitorsByDate(@Param("date") LocalDate date);

    @Select("""
            SELECT COALESCE(SUM(ticket_count), 0) FROM reservation
            WHERE visit_date = #{date} AND status IN ('CONFIRMED', 'CHECKED_IN')
            """)
    long sumVisitorsByDate(@Param("date") LocalDate date);

    @Select("""
            SELECT COALESCE(SUM(ticket_count), 0) FROM reservation
            WHERE visit_date = #{date} AND status = 'CHECKED_IN'
            """)
    long sumCheckedVisitorsByDate(@Param("date") LocalDate date);
}