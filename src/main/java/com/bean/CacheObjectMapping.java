package com.bean;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 谢俊权
 * @create 2016/7/27 21:08
 */
public class CacheObjectMapping {

    private static Map<Class<?>, Object> cacheObject = new ConcurrentHashMap<>();

    public static <T> void set(Class<?> clazz, T value){
        cacheObject.put(clazz, value);
    }

    public static <T> T get(Class<?> clazz){
        return (T) cacheObject.get(clazz);
    }
}
