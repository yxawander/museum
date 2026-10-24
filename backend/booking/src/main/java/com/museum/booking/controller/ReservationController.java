package com.museum.booking.controller;

import com.museum.booking.common.PageResult;
import com.museum.booking.common.Result;
import com.museum.booking.dto.ReservationDtos;
import com.museum.booking.security.UserContext;
import com.museum.booking.service.ReservationService;
import com.museum.booking.vo.ReservationVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 游客端预约接口。
 */
@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @GetMapping
    public Result<PageResult<ReservationVO>> myReservations(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        return Result.ok(reservationService.myPage(UserContext.require().id(), page, size, status));
    }

    @GetMapping("/{id}")
    public Result<ReservationVO> detail(@PathVariable Long id) {
        return Result.ok(reservationService.detail(id, UserContext.require()));
    }

    @PostMapping
    public Result<ReservationVO> create(@Valid @RequestBody ReservationDtos.CreateRequest request) {
        return Result.ok(reservationService.create(UserContext.require().id(), request));
    }

    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id,
                               @Valid @RequestBody(required = false) ReservationDtos.CancelRequest request) {
        reservationService.cancel(UserContext.require().id(), id,
                request == null ? null : request.reason(), false);
        return Result.ok();
    }
}