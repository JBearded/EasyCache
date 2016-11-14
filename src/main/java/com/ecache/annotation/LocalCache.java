package com.ecache.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author 谢俊权
 * @create 2016/7/17 17:18
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface LocalCache{

    /**
     * 缓存key, 默认是类名+方法名
     * 你可以使用$1,{$1},{$1.fieldName}的方式来觉得哪个参数值作为key的一部分
     * @return
     */
    String key() default "";

    int expire() default 300;
}
