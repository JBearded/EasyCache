package com.bean;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 谢俊权
 * @create 2016/7/17 17:58
 */
public class BeanFactory {

    private static Map<String, Object> map = new ConcurrentHashMap<>();

    public static <T> void set(T object){
        map.put(object.getClass().getName(), object);
    }

    public static <T> T get(Class<T> clazz){
        return (T) map.get(clazz.getName());
    }
}
