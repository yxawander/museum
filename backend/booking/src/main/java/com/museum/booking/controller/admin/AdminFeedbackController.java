package com.museum.booking.controller.admin;

import com.museum.booking.common.PageResult;
import com.museum.booking.common.Result;
import com.museum.booking.dto.AdminDtos;
import com.museum.booking.security.RequireRole;
import com.museum.booking.service.FeedbackService;
import com.museum.booking.vo.FeedbackVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台意见反馈管理接口。
 */
@RestController
@RequestMapping("/api/admin/feedbacks")
@RequiredArgsConstructor
@RequireRole("ADMIN")
public class AdminFeedbackController {

    private final FeedbackService feedbackService;

    @GetMapping
    public Result<PageResult<FeedbackVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword) {
        return Result.ok(feedbackService.adminPage(page, size, status, type, keyword));
    }

    @PostMapping("/{id}/reply")
    public Result<Void> reply(@PathVariable Long id, @Valid @RequestBody AdminDtos.ReplyForm form) {
        feedbackService.reply(id, form.reply());
        return Result.ok();
    }
}