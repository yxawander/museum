package com.museum.booking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.museum.booking.entity.Announcement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AnnouncementMapper extends BaseMapper<Announcement> {

    @Update("UPDATE announcement SET view_count = view_count + 1 WHERE id = #{id}")
    int incrementViewCount(@Param("id") Long id);
}