package com.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * @author 谢俊权
 * @create 2016/7/17 17:19
 */
@Target(ElementType.METHOD)
public @interface
RemoteCache {

   String key();
}
