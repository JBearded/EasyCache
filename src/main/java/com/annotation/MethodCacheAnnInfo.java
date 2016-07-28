package com.annotation;

import com.cache.AbstractCache;

import java.lang.reflect.Method;

/**
 * @author 谢俊权
 * @create 2016/7/25 16:56
 */
public class MethodCacheAnnInfo {

    private Method method;
    private Class<? extends AbstractCache> cacheClazz;
    private String key;
    private int expireTime;

    public MethodCacheAnnInfo() {
    }

    public MethodCacheAnnInfo(Method method, Class<? extends AbstractCache> cacheClazz, String key, int expireTime) {
        this.method = method;
        this.cacheClazz = cacheClazz;
        this.key = key;
        this.expireTime = expireTime;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Class<? extends AbstractCache> getCacheClazz() {
        return cacheClazz;
    }

    public void setCacheClazz(Class<? extends AbstractCache> cacheClazz) {
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
