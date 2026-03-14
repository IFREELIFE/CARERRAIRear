package com.endcareerai.platform.common;

import lombok.Data;

/**
 * 统一响应包装类
 * 所有 API 接口的返回数据均使用此类包装，包含状态码、消息和数据体
 *
 * @param <T> 响应数据类型
 */
@Data
public class Result<T> {
    /** 响应状态码（200=成功，400=业务错误，500=系统错误） */
    private int code;
    /** 响应消息 */
    private String message;
    /** 响应数据体 */
    private T data;

    /**
     * 创建成功响应（带数据）
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return 成功响应对象
     */
    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.setCode(200);
        r.setMessage("success");
        r.setData(data);
        return r;
    }

    /**
     * 创建成功响应（无数据）
     *
     * @param <T> 数据类型
     * @return 成功响应对象
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 创建错误响应（指定状态码和消息）
     *
     * @param code    错误状态码
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 错误响应对象
     */
    public static <T> Result<T> error(int code, String message) {
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setMessage(message);
        return r;
    }

    /**
     * 创建错误响应（默认500状态码）
     *
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 错误响应对象
     */
    public static <T> Result<T> error(String message) {
        return error(500, message);
    }
}
