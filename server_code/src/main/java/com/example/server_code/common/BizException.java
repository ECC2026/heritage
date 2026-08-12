package com.example.server_code.common;

/**
 * 业务异常，携带 HTTP 风格错误码，供 Service 向 Controller 传递错误。
 */
public class BizException extends RuntimeException {
    private final Integer code;

    public BizException(String message) {
        this(500, message);
    }

    public BizException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}
