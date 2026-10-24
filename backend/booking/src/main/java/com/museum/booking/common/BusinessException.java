package com.museum.booking.common;

import lombok.Getter;

/**
 * 业务异常。code 与前端约定：400 参数错误、401 未登录、403 无权限、404 不存在、409 业务冲突。
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message) {
        this(400, message);
    }
}