package com.museum.booking.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 认证与个人资料相关请求体。
 */
public final class AuthDtos {

    private AuthDtos() {
    }

    public record LoginRequest(
            @NotBlank(message = "请输入用户名") String username,
            @NotBlank(message = "请输入密码") String password) {
    }

    public record RegisterRequest(
            @NotBlank(message = "请输入用户名")
            @Pattern(regexp = "^[a-zA-Z0-9_]{4,20}$", message = "用户名为 4-20 位字母、数字或下划线")
            String username,
            @NotBlank(message = "请输入密码")
            @Size(min = 6, max = 20, message = "密码长度为 6-20 位")
            String password,
            @NotBlank(message = "请再次输入密码") String confirmPassword,
            @NotBlank(message = "请输入真实姓名")
            @Size(max = 20, message = "姓名不能超过 20 个字符")
            String realName,
            @NotBlank(message = "请输入手机号")
            @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入正确的 11 位手机号")
            String phone,
            @Email(message = "邮箱格式不正确") String email,
            @Pattern(regexp = "^$|^\\d{17}[\\dXx]$", message = "身份证号格式不正确") String idCard) {
    }

    public record ProfileRequest(
            @NotBlank(message = "请输入真实姓名")
            @Size(max = 20, message = "姓名不能超过 20 个字符")
            String realName,
            @NotBlank(message = "请输入手机号")
            @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入正确的 11 位手机号")
            String phone,
            @Email(message = "邮箱格式不正确") String email,
            @Pattern(regexp = "^$|^\\d{17}[\\dXx]$", message = "身份证号格式不正确") String idCard,
            String avatar) {
    }

    public record PasswordRequest(
            @NotBlank(message = "请输入原密码") String oldPassword,
            @NotBlank(message = "请输入新密码")
            @Size(min = 6, max = 20, message = "新密码长度为 6-20 位")
            String newPassword,
            @NotBlank(message = "请再次输入新密码") String confirmPassword) {
    }
}