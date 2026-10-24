package com.museum.booking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.museum.booking.entity.VisitSlot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface VisitSlotMapper extends BaseMapper<VisitSlot> {

    /**
     * 原子扣减库存：只有余量充足时才更新成功，避免并发超卖。
     */
    @Update("""
            UPDATE visit_slot
            SET booked_count = booked_count + #{count}
            WHERE id = #{slotId}
              AND status = 'OPEN'
              AND booked_count + #{count} <= capacity
            """)
    int reserveCapacity(@Param("slotId") Long slotId, @Param("count") int count);

    /**
     * 取消预约时归还库存。
     */
    @Update("""
            UPDATE visit_slot
            SET booked_count = GREATEST(booked_count - #{count}, 0)
            WHERE id = #{slotId}
            """)
    int releaseCapacity(@Param("slotId") Long slotId, @Param("count") int count);

    @Select("""
            SELECT COALESCE(SUM(capacity), 0) AS capacity,
                   COALESCE(SUM(booked_count), 0) AS booked
            FROM visit_slot
            WHERE visit_date = #{date}
            """)
    Map<String, Object> sumCapacityByDate(@Param("date") LocalDate date);

    @Select("""
            SELECT visit_date, SUM(capacity) AS capacity, SUM(booked_count) AS booked
            FROM visit_slot
            WHERE visit_date BETWEEN #{start} AND #{end}
            GROUP BY visit_date
            ORDER BY visit_date
            """)
    List<Map<String, Object>> capacityByDateRange(@Param("start") LocalDate start, @Param("end") LocalDate end);
}