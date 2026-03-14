package com.endcareerai.platform.common;

import lombok.Getter;

/**
 * 业务异常类
 * 用于在业务逻辑中抛出可预期的异常，由全局异常处理器统一捕获并返回给前端
 */
@Getter
public class BusinessException extends RuntimeException {
    /** HTTP 状态码，默认400 */
    private final int code;

    /**
     * 创建业务异常（指定状态码和消息）
     *
     * @param code    HTTP 状态码
     * @param message 错误消息
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 创建业务异常（默认400状态码）
     *
     * @param message 错误消息
     */
    public BusinessException(String message) {
        this(400, message);
    }
}
