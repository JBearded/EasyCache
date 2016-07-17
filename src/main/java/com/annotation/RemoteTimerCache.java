package com.annotation;

/**
 * @author 谢俊权
 * @create 2016/7/17 17:20
 */
public @interface RemoteTimerCache {

    String key();

    int delay() default 0;

    int interval() default 300;
}
