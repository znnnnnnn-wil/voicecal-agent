package com.voicecal.common.exception;

/**
 * 资源不存在异常，用于统一返回 404 响应。
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
