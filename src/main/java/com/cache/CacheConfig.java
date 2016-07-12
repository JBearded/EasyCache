package com.cache;

/**
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

    public int getDefaultExpireSeconds() {
        return defaultExpireSeconds;
    }

    public void setDefaultExpireSeconds(int defaultExpireSeconds) {
        this.defaultExpireSeconds = defaultExpireSeconds;
    }

    public int getSchedulerCorePoolSize() {
        return schedulerCorePoolSize;
    }

    public void setSchedulerCorePoolSize(int schedulerCorePoolSize) {
        this.schedulerCorePoolSize = schedulerCorePoolSize;
    }

    public int getRetryRegisterDelayMillisSecond() {
        return retryRegisterDelayMillisSecond;
    }

    public void setRetryRegisterDelayMillisSecond(int retryRegisterDelayMillisSecond) {
        this.retryRegisterDelayMillisSecond = retryRegisterDelayMillisSecond;
    }
}
