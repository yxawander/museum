package com.museum.booking.controller.admin;

import com.museum.booking.common.PageResult;
import com.museum.booking.common.Result;
import com.museum.booking.dto.AdminDtos;
import com.museum.booking.entity.Exhibition;
import com.museum.booking.security.RequireRole;
import com.museum.booking.service.ExhibitionService;
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
 * 后台展览管理接口。
 */
@RestController
@RequestMapping("/api/admin/exhibitions")
@RequiredArgsConstructor
@RequireRole("ADMIN")
public class AdminExhibitionController {

    private final ExhibitionService exhibitionService;

    @GetMapping
    public Result<PageResult<Exhibition>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status) {
        return Result.ok(exhibitionService.adminPage(page, size, keyword, category, status));
    }

    @GetMapping("/{id}")
    public Result<Exhibition> detail(@PathVariable Long id) {
        return Result.ok(exhibitionService.get(id));
    }

    @PostMapping
    public Result<Exhibition> create(@Valid @RequestBody AdminDtos.ExhibitionForm form) {
        return Result.ok(exhibitionService.create(form));
    }

    @PutMapping("/{id}")
    public Result<Exhibition> update(@PathVariable Long id, @Valid @RequestBody AdminDtos.ExhibitionForm form) {
        return Result.ok(exhibitionService.update(id, form));
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        exhibitionService.updateStatus(id, status);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        exhibitionService.delete(id);
        return Result.ok();
    }
}