package com.ecache.exception;

/**
 * bean容器中没有找到内置缓存对象
 *
 * @author xiejunquan
 * @create 2016/11/21 15:30
 */
public class InnerCacheObjectNotFoundException extends RuntimeException{

    public InnerCacheObjectNotFoundException(String message) {
        super(message);
    }

    public InnerCacheObjectNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
