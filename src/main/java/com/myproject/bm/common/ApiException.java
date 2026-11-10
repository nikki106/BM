package com.myproject.bm.common;

import lombok.Getter;

/**
 * 业务异常, 由全局异常处理器转成 { code, msg }
 */
@Getter
public class ApiException extends RuntimeException {

    private final int code;

    public ApiException(String message) {
        this(500, message);
    }

    public ApiException(int code, String message) {
        super(message);
        this.code = code;
    }
}
