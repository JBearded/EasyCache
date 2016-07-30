package com.annotation;

import com.cache.AbstractCache;

import java.lang.reflect.Method;

/**
 * @author 谢俊权
 * @create 2016/7/25 16:56
 */
public class MethodCacheAnnInfo {

    /**
     * 被注解的方法
     */
    private Method method;

    /**
     * 注解方法使用的缓存类型
     */
    private Class<? extends AbstractCache> cacheClazz;

    /**
     * 注解方法使用的key
     */
    private String key;

    /**
     * 注解方法使用的过期时间
     */
    private int expiredSeconds;

    private boolean avoidOverload;

    public MethodCacheAnnInfo() {
    }

    public MethodCacheAnnInfo(Method method, Class<? extends AbstractCache> cacheClazz, String key, int expiredSeconds, boolean avoidOverload) {
        this.method = method;
        this.cacheClazz = cacheClazz;
        this.key = key;
        this.expiredSeconds = expiredSeconds;
        this.avoidOverload = avoidOverload;
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

    public int getExpiredSeconds() {
        return expiredSeconds;
    }

    public void setExpiredSeconds(int expiredSeconds) {
        this.expiredSeconds = expiredSeconds;
    }

    public boolean isAvoidOverload() {
        return avoidOverload;
    }

    public void setAvoidOverload(boolean avoidOverload) {
        this.avoidOverload = avoidOverload;
    }
}
