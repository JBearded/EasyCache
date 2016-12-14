package com.ecache.annotation;

import com.ecache.*;

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

    Class<? extends EasyCache> instance() default NullCacheInstance.class;

    String key() default "";

    int expired() default 300;
}
