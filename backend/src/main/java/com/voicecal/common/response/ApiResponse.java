package com.voicecal.common.response;

import java.time.OffsetDateTime;

/**
 * 后端统一接口响应结构，用于保持前后端交互格式一致。
 *
 * @param <T> 响应数据类型
 */
@SuppressWarnings("unused")
public class ApiResponse<T> {

    private final boolean success;
    private final String code;
    private final String message;
    private final T data;
    private final OffsetDateTime timestamp;

    private ApiResponse(boolean success, String code, String message, T data) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = OffsetDateTime.now();
    }

    /**
     * 创建成功响应。
     *
     * @param data 响应数据
     * @param <T> 响应数据类型
     * @return 成功响应对象
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "OK", "请求成功", data);
    }

    /**
     * 创建带自定义提示信息的成功响应。
     *
     * @param message 成功提示信息
     * @param data 响应数据
     * @param <T> 响应数据类型
     * @return 成功响应对象
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, "OK", message, data);
    }

    /**
     * 创建不包含数据的失败响应。
     *
     * @param code 业务错误码
     * @param message 错误提示信息
     * @param <T> 响应数据类型
     * @return 失败响应对象
     */
    public static <T> ApiResponse<T> fail(String code, String message) {
        return new ApiResponse<>(false, code, message, null);
    }

    /**
     * 创建包含错误详情数据的失败响应。
     *
     * @param code 业务错误码
     * @param message 错误提示信息
     * @param data 错误详情数据
     * @param <T> 响应数据类型
     * @return 失败响应对象
     */
    public static <T> ApiResponse<T> fail(String code, String message, T data) {
        return new ApiResponse<>(false, code, message, data);
    }

    /**
     * 返回请求是否处理成功。
     *
     * @return 成功返回 true，失败返回 false
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * 返回响应码。
     *
     * @return 响应码
     */
    public String getCode() {
        return code;
    }

    /**
     * 返回响应提示信息。
     *
     * @return 响应提示信息
     */
    public String getMessage() {
        return message;
    }

    /**
     * 返回响应数据。
     *
     * @return 响应数据
     */
    public T getData() {
        return data;
    }

    /**
     * 返回响应生成时间。
     *
     * @return 响应生成时间
     */
    public OffsetDateTime getTimestamp() {
        return timestamp;
    }
}
