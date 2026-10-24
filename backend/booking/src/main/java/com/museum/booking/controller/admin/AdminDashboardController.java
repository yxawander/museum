package com.museum.booking.controller.admin;

import com.museum.booking.common.Result;
import com.museum.booking.security.RequireRole;
import com.museum.booking.service.DashboardService;
import com.museum.booking.vo.DashboardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台数据看板接口。
 */
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@RequireRole("ADMIN")
public class AdminDashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public Result<DashboardVO> overview() {
        return Result.ok(dashboardService.overview());
    }
}