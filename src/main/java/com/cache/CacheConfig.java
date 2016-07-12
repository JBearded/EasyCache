package com.cache;

/**
 * 缓存的通用配置
 * @author 谢俊权
 * @create 2016/7/12 12:23
 */
public class CacheConfig {

    private int defaultExpireSeconds = 60 * 60 * 24;

    private int schedulerCorePoolSize = 64;

    private int retryRegisterDelayMillisSecond = 1000 * 2;

    public CacheConfig() {
    }

    public CacheConfig(int defaultExpireSeconds, int schedulerCorePoolSize, int retryRegisterDelayMillisSecond) {
        this.defaultExpireSeconds = defaultExpireSeconds;
        this.schedulerCorePoolSize = schedulerCorePoolSize;
        this.retryRegisterDelayMillisSecond = retryRegisterDelayMillisSecond;
    }

    /**
     * 获取默认的缓存过期时间
     * @return
     */
    public int getDefaultExpireSeconds() {
        return defaultExpireSeconds;
    }

    /**
     * 设置默认的缓存过期时间
     * @param defaultExpireSeconds
     */
    public void setDefaultExpireSeconds(int defaultExpireSeconds) {
        this.defaultExpireSeconds = defaultExpireSeconds;
    }

    /**
     * 获取定时器的线程池数量
     * @return
     */
    public int getSchedulerCorePoolSize() {
        return schedulerCorePoolSize;
    }

    /**
     * 设置定时器的线程池数量
     * @param schedulerCorePoolSize
     */
    public void setSchedulerCorePoolSize(int schedulerCorePoolSize) {
        this.schedulerCorePoolSize = schedulerCorePoolSize;
    }

    /**
     * 获取重新注册缓存策略的延迟时间
     * @return
     */
    public int getRetryRegisterDelayMillisSecond() {
        return retryRegisterDelayMillisSecond;
    }

    /**
     * 设置重新注册缓存策略的延迟时间
     * @param retryRegisterDelayMillisSecond
     */
    public void setRetryRegisterDelayMillisSecond(int retryRegisterDelayMillisSecond) {
        this.retryRegisterDelayMillisSecond = retryRegisterDelayMillisSecond;
    }
}
