package com.ecache.annotation;

import com.ecache.EasyCache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author 谢俊权
 * @create 2016/8/5 10:15
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Cache {

    Class<? extends EasyCache> instance();

    String id() default "";

    String key() default "";

    int expire() default 300;
}
