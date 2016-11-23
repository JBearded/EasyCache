package com.ecache.exception;

/**
 * 指定的$number超出参数个数
 *
 * @author xiejunquan
 * @create 2016/11/21 15:30
 */
public class CacheKeyOutOfArgsException extends RuntimeException{

    public CacheKeyOutOfArgsException(String message) {
        super(message);
    }

    public CacheKeyOutOfArgsException(String message, Throwable cause) {
        super(message, cause);
    }
}
