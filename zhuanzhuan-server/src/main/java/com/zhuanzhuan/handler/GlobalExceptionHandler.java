package com.zhuanzhuan.handler;

import com.zhuanzhuan.constant.MessageConstant;
import com.zhuanzhuan.exception.BaseException;
import com.zhuanzhuan.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器。
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public Result<?> handleBaseException(BaseException ex) {
        log.warn("业务异常: {}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public Result<?> handleSqlIntegrityException(SQLIntegrityConstraintViolationException ex) {
        String message = ex.getMessage();
        log.warn("数据库约束异常: {}", message, ex);

        if (message != null && message.contains("Duplicate entry")) {
            String[] split = message.split(" ");
            if (split.length >= 3) {
                String duplicateValue = split[2];
                return Result.error(duplicateValue + MessageConstant.ALREADY_EXISTS);
            }
        }
        return Result.error(resolveRootCauseMessage(ex));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result<?> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        String message = resolveRootCauseMessage(ex);
        log.warn("数据完整性异常: {}", message, ex);
        return Result.error(message);
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception ex) {
        String message = resolveRootCauseMessage(ex);
        log.error("系统异常: {}", message, ex);
        return Result.error(message);
    }

    private String resolveRootCauseMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }

        String message = current.getMessage();
        if (message == null || message.trim().isEmpty()) {
            message = throwable.getMessage();
        }
        if (message == null || message.trim().isEmpty()) {
            return MessageConstant.UNKNOWN_ERROR;
        }
        return message.trim();
    }
}
