package com.museum.booking.vo;

/**
 * 登录成功返回。
 */
public record LoginVO(String token, UserVO user) {
}