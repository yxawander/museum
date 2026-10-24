package com.museum.booking.security;

/**
 * 当前登录用户信息。
 */
public record LoginUser(Long id, String username, String realName, String role) {

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    public boolean isStaff() {
        return "STAFF".equals(role) || isAdmin();
    }
}