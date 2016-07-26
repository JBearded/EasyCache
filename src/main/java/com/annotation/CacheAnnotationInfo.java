package com.annotation;

import java.lang.reflect.Method;

/**
 * @author 谢俊权
 * @create 2016/7/25 16:56
 */
public class CacheAnnotationInfo {

    private Class<?> clazz;
    private Method method;
    private Class<?> cacheClazz;
    private String key;
    private int expireTime;

    public CacheAnnotationInfo() {
    }

    public CacheAnnotationInfo(Class<?> clazz, Method method, Class<?> cacheClazz, String key, int expireTime) {
        this.clazz = clazz;
        this.method = method;
        this.cacheClazz = cacheClazz;
        this.key = key;
        this.expireTime = expireTime;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Class<?> getCacheClazz() {
        return cacheClazz;
    }

    public void setCacheClazz(Class<?> cacheClazz) {
        this.cacheClazz = cacheClazz;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(int expireTime) {
        this.expireTime = expireTime;
    }
}
