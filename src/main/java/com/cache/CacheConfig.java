package com.cache;

/**
 * 缓存的通用配置
 * @author 谢俊权
 * @create 2016/7/12 12:23
 */
public class CacheConfig {

    private int defaultExpiredSeconds = 60 * 60 * 24;

    private int schedulerCorePoolSize = 64;

    private int retryRegisterMSeconds = 1000 * 2;

    private int lockSegments = 16;

    private boolean lockIsFair = false;

    private boolean avoidServerOverload = false;

    private CacheConfig(Builder builder) {
        this.defaultExpiredSeconds = builder.defaultExpiredSeconds;
        this.schedulerCorePoolSize = builder.schedulerCorePoolSize;
        this.retryRegisterMSeconds = builder.retryRegisterMSeconds;
        this.lockSegments = builder.lockSegments;
        this.lockIsFair = builder.lockIsFair;
        this.avoidServerOverload = builder.avoidServerOverload;
    }

    public int getDefaultExpiredSeconds() {
        return defaultExpiredSeconds;
    }

    public int getSchedulerCorePoolSize() {
        return schedulerCorePoolSize;
    }

    public int getRetryRegisterMSeconds() {
        return retryRegisterMSeconds;
    }

    public int getLockSegments() {
        return lockSegments;
    }

    public boolean isLockIsFair() {
        return lockIsFair;
    }

    public boolean isAvoidServerOverload() {
        return avoidServerOverload;
    }

    public static class Builder{

        private int defaultExpiredSeconds = 60 * 60 * 24;

        private int schedulerCorePoolSize = 64;

        private int retryRegisterMSeconds = 1000 * 2;

        private int lockSegments = 16;

        private boolean lockIsFair = false;

        private boolean avoidServerOverload = false;


        public Builder() {
        }

        public Builder defaultExpiredSeconds(int expiredSeconds){
            this.defaultExpiredSeconds = expiredSeconds;
            return this;
        }

        public Builder schedulerCorePoolSize(int schedulerCorePoolSize){
            this.schedulerCorePoolSize = schedulerCorePoolSize;
            return this;
        }

        public Builder retryRegisterMSeconds(int retryRegisterMSeconds){
            this.retryRegisterMSeconds = retryRegisterMSeconds;
            return this;
        }

        public Builder lockSegments(int lockSegments){
            this.lockSegments = lockSegments;
            return this;
        }

        public Builder lockIsFair(boolean lockIsFair){
            this.lockIsFair = lockIsFair;
            return this;
        }

        public Builder avoidServerOverload(boolean avoidServerOverload){
            this.avoidServerOverload = avoidServerOverload;
            return this;
        }

        public CacheConfig build(){
            return new CacheConfig(this);
        }

    }

}
