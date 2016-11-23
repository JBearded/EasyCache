package com.ecache.exception;

/**
 * bean容器中没有找到外置缓存对象
 *
 * @author xiejunquan
 * @create 2016/11/21 15:30
 */
public class OuterCacheObjectNotFoundException extends RuntimeException{

    public OuterCacheObjectNotFoundException(String message) {
        super(message);
    }

    public OuterCacheObjectNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
