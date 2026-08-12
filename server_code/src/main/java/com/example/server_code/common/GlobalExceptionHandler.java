package com.example.server_code.common;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 统一业务异常处理：将 Service 抛出的 BizException 映射为统一返回结构。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public Result<Void> handleBizException(BizException e) {
        return Result.error(e.getCode(), e.getMessage());
    }
}
