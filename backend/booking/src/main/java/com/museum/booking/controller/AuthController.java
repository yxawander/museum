package com.museum.booking.controller;

import com.museum.booking.common.Result;
import com.museum.booking.dto.AuthDtos;
import com.museum.booking.security.UserContext;
import com.museum.booking.service.AuthService;
import com.museum.booking.vo.LoginVO;
import com.museum.booking.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证与个人中心接口。
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody AuthDtos.LoginRequest request) {
        return Result.ok(authService.login(request));
    }

    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody AuthDtos.RegisterRequest request) {
        return Result.ok(authService.register(request));
    }

    @GetMapping("/me")
    public Result<UserVO> me() {
        return Result.ok(authService.me(UserContext.require().id()));
    }

    @PutMapping("/profile")
    public Result<UserVO> updateProfile(@Valid @RequestBody AuthDtos.ProfileRequest request) {
        return Result.ok(authService.updateProfile(UserContext.require().id(), request));
    }

    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody AuthDtos.PasswordRequest request) {
        authService.changePassword(UserContext.require().id(), request);
        return Result.ok();
    }
}