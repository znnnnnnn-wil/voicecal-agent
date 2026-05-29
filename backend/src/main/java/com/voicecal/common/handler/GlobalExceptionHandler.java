package com.voicecal.common.handler;

import com.voicecal.common.enums.ResultCodeEnum;
import com.voicecal.common.exception.CustomException;
import com.voicecal.common.response.ApiResponse;
import com.voicecal.common.exception.ResourceNotFoundException;
import jakarta.validation.ConstraintViolationException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * 全局异常处理器，负责将后端异常转换为统一响应格式。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理请求体参数校验失败异常。
     *
     * @param exception 参数校验异常
     * @return 包含字段校验错误的统一响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception
    ) {
        Map<String, String> errors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        return ResponseEntity.badRequest()
                .body(ApiResponse.fail(ResultCodeEnum.PARAMS_ERROR.getCode(), "请求参数校验失败", errors));
    }

    /**
     * 处理路径参数或查询参数校验失败异常。
     *
     * @param exception 约束校验异常
     * @return 包含校验失败提示的统一响应
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<String>> handleConstraintViolation(ConstraintViolationException exception) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.fail(ResultCodeEnum.PARAMS_ERROR.getCode(), exception.getMessage()));
    }

    /**
     * 处理缺少必填查询参数的异常。
     *
     * @param exception 缺少查询参数异常
     * @return 包含缺失参数提示的统一响应
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<String>> handleMissingServletRequestParameter(
            MissingServletRequestParameterException exception
    ) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.fail(
                        ResultCodeEnum.MISS_PARAMS.getCode(),
                        exception.getParameterName() + " 参数不能为空"
                ));
    }

    /**
     * 处理查询参数类型转换失败异常。
     *
     * @param exception 参数类型转换异常
     * @return 包含参数格式错误提示的统一响应
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<String>> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException exception
    ) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.fail(
                        ResultCodeEnum.PARAMS_ERROR.getCode(),
                        exception.getName() + " 参数格式不正确"
                ));
    }

    /**
     * 处理请求体格式错误或无法解析的异常。
     *
     * @return 包含请求体格式错误提示的统一响应
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<String>> handleHttpMessageNotReadable() {
        return ResponseEntity.badRequest()
                .body(ApiResponse.fail(ResultCodeEnum.JSON_WRONG.getCode(), "请求体格式不正确"));
    }

    /**
     * 处理自定义业务异常。
     *
     * @param exception 自定义业务异常
     * @return 包含业务错误码的统一响应
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<String>> handleCustomException(CustomException exception) {
        return ResponseEntity.status(exception.getHttpStatus())
                .body(ApiResponse.fail(exception.getCode(), exception.getMessage()));
    }

    /**
     * 处理业务参数不合法异常。
     *
     * @param exception 非法参数异常
     * @return 包含业务参数错误提示的统一响应
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<String>> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.fail(ResultCodeEnum.PARAMS_ERROR.getCode(), exception.getMessage()));
    }

    /**
     * 处理资源不存在异常。
     *
     * @param exception 资源不存在异常
     * @return 包含资源不存在提示的统一响应
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleResourceNotFound(ResourceNotFoundException exception) {
        return ResponseEntity.status(exception.getHttpStatus())
                .body(ApiResponse.fail(exception.getCode(), exception.getMessage()));
    }

    /**
     * 处理未预期的系统异常。
     *
     * @return 隐藏内部堆栈细节的统一响应
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleException() {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail(ResultCodeEnum.FAIL.getCode(), "服务器内部错误"));
    }
}
