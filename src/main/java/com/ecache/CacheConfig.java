package com.ecache;

/**
 * 缓存的通用配置
 * @author 谢俊权
 * @create 2016/7/12 12:23
 */
public class CacheConfig {

    /**
     * 默认过期时间
     */
    private int defaultExpiredSeconds = 60 * 60 * 24;

    /**
     * 定时器的核心线程数
     */
    private int schedulerCorePoolSize = 64;

    /**
     * 注册失败后, 重新注册的间隔时间
     */
    private int retryRegisterMSeconds = 1000 * 2;

    /**
     * 分段锁的段数, 其值限制在32<=lockSegments<=1024
     */
    private int lockSegments = 32;

    /**
     * 是否是公平锁
      */
    private boolean lockIsFair = false;

    /**
     * 是否防止数据源服务端过载
     */
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
