package com.museum.booking.controller.admin;

import com.museum.booking.common.Result;
import com.museum.booking.dto.ReservationDtos;
import com.museum.booking.security.RequireRole;
import com.museum.booking.security.UserContext;
import com.museum.booking.service.ReservationService;
import com.museum.booking.vo.ReservationVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 现场核销接口（管理员、检票员可用）。
 */
@RestController
@RequestMapping("/api/admin/checkin")
@RequiredArgsConstructor
@RequireRole({"ADMIN", "STAFF"})
public class AdminCheckinController {

    private final ReservationService reservationService;

    @GetMapping("/search")
    public Result<ReservationVO> search(@RequestParam String code) {
        return Result.ok(reservationService.preview(code));
    }

    @PostMapping
    public Result<ReservationVO> checkin(@Valid @RequestBody ReservationDtos.CheckinRequest request) {
        return Result.ok(reservationService.checkin(request.code(), UserContext.require()));
    }
}