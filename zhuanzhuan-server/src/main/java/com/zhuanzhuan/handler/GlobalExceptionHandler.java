package com.zhuanzhuan.handler;


import com.zhuanzhuan.constant.MessageConstant;
import com.zhuanzhuan.exception.BaseException;
import com.zhuanzhuan.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice// 声明这是一个全局异常处理组件，并要求返回 JSON 格式
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 1. 专门捕获业务异常 (你自定义的异常)
     * 当 Controller 中抛出 BaseException 及其子类时，会进入这里
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(BaseException ex){
            log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    @ExceptionHandler
    public Result exceptionHandler(SQLIntegrityConstraintViolationException exception){
        // Duplicate entry 'zhangsan' for key 'employee.idx_username'
        String message = exception.getMessage();
        if(message.contains("Duplicate entry")){
            String[] split = message.split(" ");
            String username=split[2];
            String msg= username+ MessageConstant.ALREADY_EXISTS;
            return Result.error(msg);
        }else {
            return Result.error(MessageConstant.UNKNOWN_ERROR);
        }
    }

}
