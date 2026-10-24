package com.museum.booking.controller.admin;

import com.museum.booking.common.PageResult;
import com.museum.booking.common.Result;
import com.museum.booking.dto.AdminDtos;
import com.museum.booking.security.RequireRole;
import com.museum.booking.security.UserContext;
import com.museum.booking.service.UserService;
import com.museum.booking.vo.UserVO;
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
 * 后台用户管理接口。
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@RequireRole("ADMIN")
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    public Result<PageResult<UserVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer status) {
        return Result.ok(userService.page(page, size, keyword, role, status));
    }

    @GetMapping("/{id}")
    public Result<UserVO> detail(@PathVariable Long id) {
        return Result.ok(userService.get(id));
    }

    @PostMapping
    public Result<UserVO> create(@Valid @RequestBody AdminDtos.UserCreateForm form) {
        return Result.ok(userService.create(form));
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody AdminDtos.StatusForm form) {
        userService.updateStatus(id, form.status(), UserContext.require().id());
        return Result.ok();
    }

    @PutMapping("/{id}/password")
    public Result<Void> resetPassword(@PathVariable Long id,
                                      @Valid @RequestBody AdminDtos.ResetPasswordForm form) {
        userService.resetPassword(id, form.newPassword());
        return Result.ok();
    }
}