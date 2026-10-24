package com.museum.booking.controller.admin;

import com.museum.booking.common.PageResult;
import com.museum.booking.common.Result;
import com.museum.booking.dto.AdminDtos;
import com.museum.booking.entity.Announcement;
import com.museum.booking.security.RequireRole;
import com.museum.booking.service.AnnouncementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台公告管理接口。
 */
@RestController
@RequestMapping("/api/admin/announcements")
@RequiredArgsConstructor
@RequireRole("ADMIN")
public class AdminAnnouncementController {

    private final AnnouncementService announcementService;

    @GetMapping
    public Result<PageResult<Announcement>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        return Result.ok(announcementService.adminPage(page, size, category, keyword, status));
    }

    @GetMapping("/{id}")
    public Result<Announcement> detail(@PathVariable Long id) {
        return Result.ok(announcementService.get(id));
    }

    @PostMapping
    public Result<Announcement> create(@Valid @RequestBody AdminDtos.AnnouncementForm form) {
        return Result.ok(announcementService.create(form));
    }

    @PutMapping("/{id}")
    public Result<Announcement> update(@PathVariable Long id,
                                       @Valid @RequestBody AdminDtos.AnnouncementForm form) {
        return Result.ok(announcementService.update(id, form));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        announcementService.delete(id);
        return Result.ok();
    }
}