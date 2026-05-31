package com.voicecal.common.exception;

import com.voicecal.common.enums.ResultCodeEnum;
import org.springframework.http.HttpStatus;

/**
 * 自定义业务异常，用于携带统一响应状态码。
 */
public class CustomException extends RuntimeException {

    private final ResultCodeEnum resultCodeEnum;

    protected CustomException(ResultCodeEnum resultCodeEnum) {
        super(resultCodeEnum.getMessage());
        this.resultCodeEnum = resultCodeEnum;
    }

    protected CustomException(ResultCodeEnum resultCodeEnum, String message) {
        super(message);
        this.resultCodeEnum = resultCodeEnum;
    }

    /**
     * 创建自定义异常。
     *
     * @param resultCodeEnum 响应状态码
     * @return 自定义异常
     */
    public static CustomException create(ResultCodeEnum resultCodeEnum) {
        return new CustomException(resultCodeEnum);
    }

    /**
     * 创建带自定义消息的自定义异常。
     *
     * @param resultCodeEnum 响应状态码
     * @param message 自定义消息
     * @return 自定义异常
     */
    public static CustomException create(ResultCodeEnum resultCodeEnum, String message) {
        return new CustomException(resultCodeEnum, message);
    }

    public String getCode() {
        return resultCodeEnum.getCode();
    }

    public HttpStatus getHttpStatus() {
        return resultCodeEnum.getHttpStatus();
    }
}
