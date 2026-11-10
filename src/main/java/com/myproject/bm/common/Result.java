package com.myproject.bm.common;

import lombok.Data;

/**
 * 统一返回格式: { code, msg, data }
 * code = 200 成功, 401 未登录/登录过期, 403 无权限, 500 业务或系统错误
 */
@Data
public class Result<T> {

    private int code;
    private String msg;
    private T data;

    public Result() {
    }

    public Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> Result<T> ok(T data) {
        return new Result<>(200, "success", data);
    }

    public static <T> Result<T> fail(String msg) {
        return new Result<>(500, msg, null);
    }

    public static <T> Result<T> fail(int code, String msg) {
        return new Result<>(code, msg, null);
    }
}
