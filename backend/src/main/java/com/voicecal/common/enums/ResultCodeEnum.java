package com.voicecal.common.enums;

import org.springframework.http.HttpStatus;

/**
 * 统一响应状态码枚举。
 */
public enum ResultCodeEnum {
    /**
     * 成功。
     */
    SUCCESS("00000", "成功", HttpStatus.OK),

    /**
     * 缺少参数。
     */
    MISS_PARAMS("A0410", "缺少参数", HttpStatus.BAD_REQUEST),

    /**
     * 用户输入内容非法。
     */
    PARAMS_ERROR("A0430", "用户输入内容非法", HttpStatus.BAD_REQUEST),

    /**
     * JSON 解析失败。
     */
    JSON_WRONG("A0427", "JSON 解析失败", HttpStatus.BAD_REQUEST),

    /**
     * 请求资源不存在。
     */
    RESOURCE_NOT_FOUND("A0404", "请求资源不存在", HttpStatus.NOT_FOUND),

    /**
     * 日程不存在。
     */
    CALENDAR_EVENT_NOT_FOUND("A1404", "日程不存在", HttpStatus.NOT_FOUND),

    /**
     * 日程时间冲突。
     */
    CALENDAR_EVENT_CONFLICT("A1409", "日程时间冲突", HttpStatus.BAD_REQUEST),

    /**
     * 用户请求服务异常。
     */
    FAIL("A0500", "用户请求服务异常", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ResultCodeEnum(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
