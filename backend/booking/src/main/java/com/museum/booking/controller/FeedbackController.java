package com.museum.booking.controller;

import com.museum.booking.common.PageResult;
import com.museum.booking.common.Result;
import com.museum.booking.dto.ReservationDtos;
import com.museum.booking.security.UserContext;
import com.museum.booking.service.FeedbackService;
import com.museum.booking.vo.FeedbackVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 游客端意见反馈接口。
 */
@RestController
@RequestMapping("/api/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping
    public Result<Void> create(@Valid @RequestBody ReservationDtos.FeedbackRequest request) {
        feedbackService.create(UserContext.require().id(), request);
        return Result.ok();
    }

    @GetMapping("/my")
    public Result<PageResult<FeedbackVO>> my(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(feedbackService.myPage(UserContext.require().id(), page, size));
    }
}