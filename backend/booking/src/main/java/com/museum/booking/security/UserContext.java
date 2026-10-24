package com.museum.booking.security;

import com.museum.booking.common.BusinessException;

/**
 * 基于 ThreadLocal 的当前登录用户上下文。
 */
public final class UserContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    public static LoginUser require() {
        LoginUser user = HOLDER.get();
        if (user == null) {
            throw new BusinessException(401, "请先登录");
        }
        return user;
    }

    public static void clear() {
        HOLDER.remove();
    }
}