package com.ecache.annotation;

import com.ecache.AbstractCache;
import com.ecache.CacheInterface;

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
    private Class<? extends AbstractCache> innerCacheClazz;
    private Class<? extends CacheInterface> outerCacheClazz;

    /**
     * 缓存实例id
     */
    private String id;

    /**
     * 注解方法使用的key
     */
    private String key;

    /**
     * 注解方法使用的过期时间
     */
    private int expiredSeconds;

    private boolean avoidOverload;

    private boolean isInnerCache;

    public MethodCacheAnnInfo(Builder builder) {
        this.method = builder.method;
        this.innerCacheClazz = builder.innerCacheClazz;
        this.outerCacheClazz = builder.outerCacheClazz;
        this.id = builder.id;
        this.key = builder.key;
        this.expiredSeconds = builder.expiredSeconds;
        this.avoidOverload = builder.avoidOverload;
    }

    public Method getMethod() {
        return method;
    }

    public Class<? extends AbstractCache> getInnerCacheClazz() {
        return innerCacheClazz;
    }

    public Class<? extends CacheInterface> getOuterCacheClazz() {
        return outerCacheClazz;
    }

    public String getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public int getExpiredSeconds() {
        return expiredSeconds;
    }

    public boolean isAvoidOverload() {
        return avoidOverload;
    }

    public boolean isInnerCache() {
        return (this.innerCacheClazz != null) ? true : false;
    }

    public boolean isOuterCache(){
        return (this.outerCacheClazz != null) ? true : false;
    }

    @Override
    public String toString() {
        return new StringBuilder(super.toString())
                .append("_").append(id)
                .append("_").append(key)
                .append("_").append(method)
                .append("_").append(expiredSeconds)
                .append("_").append(isInnerCache)
                .append("_").append(avoidOverload)
                .toString();
    }

    public static class Builder{

        /**
         * 被注解的方法
         */
        private Method method;

        /**
         * 注解方法使用的缓存类型
         */
        private Class<? extends AbstractCache> innerCacheClazz;
        private Class<? extends CacheInterface> outerCacheClazz;

        /**
         * 缓存实例id
         */
        private String id;

        /**
         * 注解方法使用的key
         */
        private String key;

        /**
         * 注解方法使用的过期时间
         */
        private int expiredSeconds;

        private boolean avoidOverload;

        public Builder method(Method method){
            this.method = method;
            return this;
        }

        public Builder innerCacheClazz(Class<? extends AbstractCache> cache){
            this.innerCacheClazz = cache;
            return this;
        }

        public Builder outerCacheClazz(Class<? extends CacheInterface> cache){
            this.outerCacheClazz = cache;
            return this;
        }

        public Builder id(String id){
            this.id = id;
            return this;
        }

        public Builder key(String key){
            this.key = key;
            return this;
        }

        public Builder expiredSeconds(int expiredSeconds){
            this.expiredSeconds = expiredSeconds;
            return this;
        }

        public Builder avoidOverload(boolean avoidOverload){
            this.avoidOverload = avoidOverload;
            return this;
        }

        public MethodCacheAnnInfo buil(){
            return new MethodCacheAnnInfo(this);
        }

    }


}
