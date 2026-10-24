package com.museum.booking.vo;

import com.museum.booking.entity.SysUser;

import java.time.LocalDateTime;

/**
 * 用户视图对象，敏感字段脱敏。
 */
public record UserVO(
        Long id,
        String username,
        String realName,
        String idCard,
        String phone,
        String email,
        String avatar,
        String role,
        Integer status,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt) {

    public static UserVO from(SysUser user) {
        if (user == null) {
            return null;
        }
        return new UserVO(user.getId(), user.getUsername(), user.getRealName(), maskIdCard(user.getIdCard()),
                user.getPhone(), user.getEmail(), user.getAvatar(), user.getRole(), user.getStatus(),
                user.getLastLoginAt(), user.getCreatedAt());
    }

    public static String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 8) {
            return idCard;
        }
        return idCard.substring(0, 6) + "********" + idCard.substring(idCard.length() - 4);
    }
}