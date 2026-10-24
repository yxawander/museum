package com.museum.booking.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.museum.booking.common.BusinessException;
import com.museum.booking.common.PageResult;
import com.museum.booking.dto.AdminDtos;
import com.museum.booking.entity.Exhibition;
import com.museum.booking.entity.Reservation;
import com.museum.booking.entity.VisitSlot;
import com.museum.booking.mapper.ExhibitionMapper;
import com.museum.booking.mapper.ReservationMapper;
import com.museum.booking.mapper.VisitSlotMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

/**
 * 展览业务服务。
 */
@Service
@RequiredArgsConstructor
public class ExhibitionService {

    private static final List<String> STATUSES = List.of("DRAFT", "PUBLISHED", "CLOSED");

    private final ExhibitionMapper exhibitionMapper;
    private final VisitSlotMapper slotMapper;
    private final ReservationMapper reservationMapper;

    public PageResult<Exhibition> publicPage(int page, int size, String keyword, String category) {
        LambdaQueryWrapper<Exhibition> wrapper = new LambdaQueryWrapper<Exhibition>()
                .eq(Exhibition::getStatus, "PUBLISHED")
                .ge(Exhibition::getEndDate, LocalDate.now())
                .select(Exhibition.class, info -> !"description".equals(info.getColumn()));
        if (StringUtils.hasText(keyword)) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(Exhibition::getTitle, kw).or().like(Exhibition::getSubtitle, kw));
        }
        if (StringUtils.hasText(category)) {
            wrapper.eq(Exhibition::getCategory, category);
        }
        wrapper.orderByDesc(Exhibition::getSortOrder).orderByDesc(Exhibition::getId);
        Page<Exhibition> result = exhibitionMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResult.of(result);
    }

    public List<Exhibition> featured(int limit) {
        LambdaQueryWrapper<Exhibition> wrapper = new LambdaQueryWrapper<Exhibition>()
                .eq(Exhibition::getStatus, "PUBLISHED")
                .ge(Exhibition::getEndDate, LocalDate.now())
                .orderByDesc(Exhibition::getSortOrder)
                .last("LIMIT " + Math.max(1, Math.min(limit, 12)));
        return exhibitionMapper.selectList(wrapper);
    }

    public Exhibition publicDetail(Long id) {
        Exhibition exhibition = exhibitionMapper.selectById(id);
        if (exhibition == null || !"PUBLISHED".equals(exhibition.getStatus())) {
            throw new BusinessException(404, "展览不存在或已下架");
        }
        exhibitionMapper.incrementViewCount(id);
        exhibition.setViewCount((exhibition.getViewCount() == null ? 0 : exhibition.getViewCount()) + 1);
        return exhibition;
    }

    public Exhibition get(Long id) {
        Exhibition exhibition = exhibitionMapper.selectById(id);
        if (exhibition == null) {
            throw new BusinessException(404, "展览不存在");
        }
        return exhibition;
    }

    public PageResult<Exhibition> adminPage(int page, int size, String keyword, String category, String status) {
        LambdaQueryWrapper<Exhibition> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(Exhibition::getTitle, kw).or().like(Exhibition::getSubtitle, kw)
                    .or().like(Exhibition::getVenue, kw));
        }
        if (StringUtils.hasText(category)) {
            wrapper.eq(Exhibition::getCategory, category);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(Exhibition::getStatus, status);
        }
        wrapper.orderByDesc(Exhibition::getSortOrder).orderByDesc(Exhibition::getId);
        return PageResult.of(exhibitionMapper.selectPage(new Page<>(page, size), wrapper));
    }

    public Exhibition create(AdminDtos.ExhibitionForm form) {
        validate(form);
        Exhibition exhibition = new Exhibition();
        applyForm(exhibition, form);
        if (!StringUtils.hasText(exhibition.getStatus())) {
            exhibition.setStatus("DRAFT");
        }
        if (exhibition.getSortOrder() == null) {
            exhibition.setSortOrder(0);
        }
        exhibition.setViewCount(0);
        exhibitionMapper.insert(exhibition);
        return exhibition;
    }

    public Exhibition update(Long id, AdminDtos.ExhibitionForm form) {
        validate(form);
        Exhibition exhibition = get(id);
        applyForm(exhibition, form);
        exhibitionMapper.updateById(exhibition);
        return exhibition;
    }

    public void updateStatus(Long id, String status) {
        if (!STATUSES.contains(status)) {
            throw new BusinessException(400, "非法的展览状态");
        }
        get(id);
        Exhibition update = new Exhibition();
        update.setId(id);
        update.setStatus(status);
        exhibitionMapper.updateById(update);
    }

    @Transactional
    public void delete(Long id) {
        get(id);
        long reservations = reservationMapper.selectCount(new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getExhibitionId, id));
        if (reservations > 0) {
            throw new BusinessException(409, "该展览已有预约记录，不能删除，可将其下架");
        }
        slotMapper.delete(new LambdaQueryWrapper<VisitSlot>().eq(VisitSlot::getExhibitionId, id));
        exhibitionMapper.deleteById(id);
    }

    private void validate(AdminDtos.ExhibitionForm form) {
        if (form.endDate().isBefore(form.startDate())) {
            throw new BusinessException(400, "结束日期不能早于开始日期");
        }
        if (!form.closeTime().isAfter(form.openTime())) {
            throw new BusinessException(400, "闭馆时间必须晚于开放时间");
        }
        if (StringUtils.hasText(form.status()) && !STATUSES.contains(form.status())) {
            throw new BusinessException(400, "非法的展览状态");
        }
    }

    private void applyForm(Exhibition exhibition, AdminDtos.ExhibitionForm form) {
        exhibition.setTitle(form.title().trim());
        exhibition.setSubtitle(form.subtitle());
        exhibition.setCategory(form.category());
        exhibition.setCoverUrl(form.coverUrl());
        exhibition.setDescription(form.description());
        exhibition.setVenue(form.venue());
        exhibition.setStartDate(form.startDate());
        exhibition.setEndDate(form.endDate());
        exhibition.setOpenTime(form.openTime());
        exhibition.setCloseTime(form.closeTime());
        exhibition.setTicketPrice(form.ticketPrice());
        exhibition.setTips(form.tips());
        if (StringUtils.hasText(form.status())) {
            exhibition.setStatus(form.status());
        }
        if (form.sortOrder() != null) {
            exhibition.setSortOrder(form.sortOrder());
        }
    }
}