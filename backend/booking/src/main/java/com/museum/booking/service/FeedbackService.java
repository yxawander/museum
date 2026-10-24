package com.museum.booking.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.museum.booking.common.BusinessException;
import com.museum.booking.common.PageResult;
import com.museum.booking.dto.ReservationDtos;
import com.museum.booking.entity.Feedback;
import com.museum.booking.entity.SysUser;
import com.museum.booking.mapper.FeedbackMapper;
import com.museum.booking.mapper.SysUserMapper;
import com.museum.booking.vo.FeedbackVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 意见反馈服务。
 */
@Service
@RequiredArgsConstructor
public class FeedbackService {

    private static final List<String> TYPES = List.of("SUGGESTION", "COMPLAINT", "PRAISE", "OTHER");

    private final FeedbackMapper feedbackMapper;
    private final SysUserMapper userMapper;

    public void create(Long userId, ReservationDtos.FeedbackRequest request) {
        if (!TYPES.contains(request.type())) {
            throw new BusinessException(400, "非法的反馈类型");
        }
        Feedback feedback = new Feedback();
        feedback.setUserId(userId);
        feedback.setType(request.type());
        feedback.setContent(request.content().trim());
        feedback.setContact(request.contact());
        feedback.setStatus("PENDING");
        feedbackMapper.insert(feedback);
    }

    public PageResult<FeedbackVO> myPage(Long userId, int page, int size) {
        Page<Feedback> result = feedbackMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Feedback>()
                        .eq(Feedback::getUserId, userId)
                        .orderByDesc(Feedback::getCreatedAt));
        return PageResult.of(toVOList(result.getRecords()), result.getTotal(), result.getCurrent(), result.getSize());
    }

    public PageResult<FeedbackVO> adminPage(int page, int size, String status, String type, String keyword) {
        LambdaQueryWrapper<Feedback> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status)) {
            wrapper.eq(Feedback::getStatus, status);
        }
        if (StringUtils.hasText(type)) {
            wrapper.eq(Feedback::getType, type);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Feedback::getContent, keyword.trim());
        }
        wrapper.orderByAsc(Feedback::getStatus).orderByDesc(Feedback::getCreatedAt);
        Page<Feedback> result = feedbackMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResult.of(toVOList(result.getRecords()), result.getTotal(), result.getCurrent(), result.getSize());
    }

    public void reply(Long id, String reply) {
        Feedback feedback = feedbackMapper.selectById(id);
        if (feedback == null) {
            throw new BusinessException(404, "反馈记录不存在");
        }
        feedbackMapper.update(null, new LambdaUpdateWrapper<Feedback>()
                .eq(Feedback::getId, id)
                .set(Feedback::getReply, reply.trim())
                .set(Feedback::getStatus, "REPLIED")
                .set(Feedback::getRepliedAt, LocalDateTime.now()));
    }

    public List<FeedbackVO> toVOList(List<Feedback> feedbacks) {
        if (feedbacks == null || feedbacks.isEmpty()) {
            return List.of();
        }
        List<Long> userIds = feedbacks.stream().map(Feedback::getUserId).distinct().toList();
        Map<Long, SysUser> users = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, Function.identity(), (a, b) -> a));
        return feedbacks.stream().map(f -> FeedbackVO.from(f, users.get(f.getUserId()))).toList();
    }
}