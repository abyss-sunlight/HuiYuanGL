package com.example.membersystem.common;

/**
 * 业务异常，用于可预期的业务错误（比如参数非法、资源不存在等）。
 */
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
