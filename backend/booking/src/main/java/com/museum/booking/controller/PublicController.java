package com.museum.booking.controller;

import com.museum.booking.common.PageResult;
import com.museum.booking.common.Result;
import com.museum.booking.entity.Announcement;
import com.museum.booking.entity.Exhibition;
import com.museum.booking.service.AnnouncementService;
import com.museum.booking.service.DashboardService;
import com.museum.booking.service.ExhibitionService;
import com.museum.booking.service.SlotService;
import com.museum.booking.vo.HomeVO;
import com.museum.booking.vo.SlotVO;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * 面向公众的展览、时段、公告与首页接口（无需登录）。
 */
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {

    private final ExhibitionService exhibitionService;
    private final SlotService slotService;
    private final AnnouncementService announcementService;
    private final DashboardService dashboardService;

    @GetMapping("/home")
    public Result<HomeVO> home() {
        return Result.ok(new HomeVO(
                exhibitionService.featured(6),
                announcementService.publicPage(1, 5, null, null).getRecords(),
                dashboardService.publicStats()));
    }

    @GetMapping("/exhibitions")
    public Result<PageResult<Exhibition>> exhibitions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "9") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category) {
        return Result.ok(exhibitionService.publicPage(page, size, keyword, category));
    }

    @GetMapping("/exhibitions/{id}")
    public Result<Exhibition> exhibition(@PathVariable Long id) {
        return Result.ok(exhibitionService.publicDetail(id));
    }

    @GetMapping("/exhibitions/{id}/slots")
    public Result<List<SlotVO>> slots(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.ok(slotService.publicList(id, startDate, endDate));
    }

    @GetMapping("/announcements")
    public Result<PageResult<Announcement>> announcements(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {
        return Result.ok(announcementService.publicPage(page, size, category, keyword));
    }

    @GetMapping("/announcements/{id}")
    public Result<Announcement> announcement(@PathVariable Long id) {
        return Result.ok(announcementService.publicDetail(id));
    }
}