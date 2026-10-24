package com.museum.booking.controller.admin;

import com.museum.booking.common.PageResult;
import com.museum.booking.common.Result;
import com.museum.booking.dto.AdminDtos;
import com.museum.booking.security.RequireRole;
import com.museum.booking.service.SlotService;
import com.museum.booking.vo.SlotVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;

/**
 * 后台参观时段管理接口。
 */
@RestController
@RequestMapping("/api/admin/slots")
@RequiredArgsConstructor
@RequireRole("ADMIN")
public class AdminSlotController {

    private final SlotService slotService;

    @GetMapping
    public Result<PageResult<SlotVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long exhibitionId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String status) {
        return Result.ok(slotService.adminPage(page, size, exhibitionId, date, status));
    }

    @PostMapping
    public Result<SlotVO> create(@Valid @RequestBody AdminDtos.SlotForm form) {
        return Result.ok(slotService.create(form));
    }

    @PutMapping("/{id}")
    public Result<SlotVO> update(@PathVariable Long id, @Valid @RequestBody AdminDtos.SlotForm form) {
        return Result.ok(slotService.update(id, form));
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        slotService.updateStatus(id, status);
        return Result.ok();
    }

    @PostMapping("/batch")
    public Result<Map<String, Integer>> batch(@Valid @RequestBody AdminDtos.BatchSlotForm form) {
        return Result.ok(Map.of("created", slotService.batchGenerate(form)));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        slotService.delete(id);
        return Result.ok();
    }
}