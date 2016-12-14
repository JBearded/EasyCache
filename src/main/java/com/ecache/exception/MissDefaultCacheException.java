package com.ecache.exception;

/**
 * @author xiejunquan
 * @create 2016/12/14 16:05
 */
public class MissDefaultCacheException extends RuntimeException{
    public MissDefaultCacheException(String message) {
        super(message);
    }

    public MissDefaultCacheException(String message, Throwable cause) {
        super(message, cause);
    }
}
