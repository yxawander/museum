package com.museum.booking.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.museum.booking.common.BusinessException;
import com.museum.booking.common.PageResult;
import com.museum.booking.dto.AdminDtos;
import com.museum.booking.entity.Announcement;
import com.museum.booking.mapper.AnnouncementMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 公告服务。
 */
@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementMapper announcementMapper;

    public PageResult<Announcement> publicPage(int page, int size, String category, String keyword) {
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<Announcement>()
                .eq(Announcement::getStatus, 1)
                .select(Announcement.class, info -> !"content".equals(info.getColumn()));
        if (StringUtils.hasText(category)) {
            wrapper.eq(Announcement::getCategory, category);
        }
        if (StringUtils.hasText(keyword)) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(Announcement::getTitle, kw).or().like(Announcement::getSummary, kw));
        }
        wrapper.orderByDesc(Announcement::getIsTop).orderByDesc(Announcement::getPublishTime)
                .orderByDesc(Announcement::getId);
        return PageResult.of(announcementMapper.selectPage(new Page<>(page, size), wrapper));
    }

    public Announcement publicDetail(Long id) {
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null || announcement.getStatus() == null || announcement.getStatus() != 1) {
            throw new BusinessException(404, "公告不存在或未发布");
        }
        announcementMapper.incrementViewCount(id);
        announcement.setViewCount((announcement.getViewCount() == null ? 0 : announcement.getViewCount()) + 1);
        return announcement;
    }

    public PageResult<Announcement> adminPage(int page, int size, String category, String keyword, Integer status) {
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(category)) {
            wrapper.eq(Announcement::getCategory, category);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Announcement::getTitle, keyword.trim());
        }
        if (status != null) {
            wrapper.eq(Announcement::getStatus, status);
        }
        wrapper.orderByDesc(Announcement::getIsTop).orderByDesc(Announcement::getCreatedAt);
        return PageResult.of(announcementMapper.selectPage(new Page<>(page, size), wrapper));
    }

    public Announcement create(AdminDtos.AnnouncementForm form) {
        Announcement announcement = new Announcement();
        applyForm(announcement, form);
        announcement.setViewCount(0);
        if (announcement.getStatus() == 1) {
            announcement.setPublishTime(LocalDateTime.now());
        }
        announcementMapper.insert(announcement);
        return announcement;
    }

    public Announcement update(Long id, AdminDtos.AnnouncementForm form) {
        Announcement announcement = get(id);
        applyForm(announcement, form);
        if (announcement.getStatus() == 1 && announcement.getPublishTime() == null) {
            announcement.setPublishTime(LocalDateTime.now());
        }
        announcementMapper.updateById(announcement);
        return announcement;
    }

    public void delete(Long id) {
        get(id);
        announcementMapper.deleteById(id);
    }

    public Announcement get(Long id) {
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            throw new BusinessException(404, "公告不存在");
        }
        return announcement;
    }

    private void applyForm(Announcement announcement, AdminDtos.AnnouncementForm form) {
        announcement.setTitle(form.title().trim());
        announcement.setSummary(form.summary());
        announcement.setContent(form.content());
        announcement.setCategory(form.category());
        announcement.setCoverUrl(form.coverUrl());
        announcement.setIsTop(form.isTop() == null ? 0 : form.isTop());
        announcement.setStatus(form.status() == null ? 0 : form.status());
    }
}