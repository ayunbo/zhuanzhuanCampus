package com.zhuanzhuan.annotation;



import com.zhuanzhuan.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解。用于标识方法需要进行公共字段填充处理
 */
@Target(ElementType.METHOD)//标记该注解加在什么位置
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoFill {

    //数据库操作类型
    OperationType value();

}
