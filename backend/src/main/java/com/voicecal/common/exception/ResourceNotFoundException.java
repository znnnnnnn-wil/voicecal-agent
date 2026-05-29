package com.voicecal.common.exception;

import com.voicecal.common.enums.ResultCodeEnum;

/**
 * 资源不存在异常，用于统一返回 404 响应。
 */
public class ResourceNotFoundException extends CustomException {

    public ResourceNotFoundException(String message) {
        super(ResultCodeEnum.RESOURCE_NOT_FOUND, message);
    }

    public ResourceNotFoundException(ResultCodeEnum resultCodeEnum, String message) {
        super(resultCodeEnum, message);
    }
}
