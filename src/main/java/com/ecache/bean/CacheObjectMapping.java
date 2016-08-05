package com.ecache.bean;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 谢俊权
 * @create 2016/7/27 21:08
 */
public class CacheObjectMapping {

    private static Map<String, Object> cacheObject = new ConcurrentHashMap<>();

    public static <T> void set(String key, T value){
        cacheObject.put(key, value);
    }

    public static <T> T get(String key){
        return (T) cacheObject.get(key);
    }
}
