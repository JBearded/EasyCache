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
    protected int defaultExpiredSeconds = 60 * 60 * 24;

    /**
     * 定时器的核心线程数
     */
    protected int schedulerCorePoolSize = 64;

    /**
     * 分段锁的段数, 其值限制在8<=lockSegments<=32
     */
    protected int lockSegments = 16;

    /**
     * 是否是公平锁
      */
    protected boolean lockIsFair = false;

    /**
     * 是否防止数据源服务端过载
     */
    protected boolean avoidServerOverload = false;

    /**
     * 本地缓存定时清除过期缓存的间隔时间
     */
    private int clearSchedulerIntervalSeconds = 60 * 60 * 24;

    protected CacheConfig(Builder builder) {
        if(builder != null){
            this.defaultExpiredSeconds = builder.defaultExpiredSeconds;
            this.schedulerCorePoolSize = builder.schedulerCorePoolSize;
            this.lockSegments = builder.lockSegments;
            this.lockIsFair = builder.lockIsFair;
            this.avoidServerOverload = builder.avoidServerOverload;
            this.clearSchedulerIntervalSeconds = builder.clearSchedulerIntervalSeconds;
        }
    }

    public int getDefaultExpiredSeconds() {
        return defaultExpiredSeconds;
    }

    public int getSchedulerCorePoolSize() {
        return schedulerCorePoolSize;
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

    public int getClearSchedulerIntervalSeconds() {
        return clearSchedulerIntervalSeconds;
    }

    public static class Builder{

        private int defaultExpiredSeconds = 60 * 60 * 24;

        private int schedulerCorePoolSize = 64;

        private int lockSegments = 16;

        private boolean lockIsFair = false;

        private boolean avoidServerOverload = false;

        private int clearSchedulerIntervalSeconds = 60 * 60 * 24;


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

        public Builder clearSchedulerIntervalSeconds(int clearSchedulerIntervalSeconds){
            this.clearSchedulerIntervalSeconds = clearSchedulerIntervalSeconds;
            return this;
        }

        public CacheConfig build(){
            return new CacheConfig(this);
        }

    }

}
