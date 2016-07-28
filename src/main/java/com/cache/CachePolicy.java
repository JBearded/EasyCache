package com.cache;

/**
 * 缓存策略
 * @author 谢俊权
 * @create 2016/7/9 17:53
 */
public class CachePolicy<T> {

    private int expireSeconds;

    private int delaySeconds;

    private int intervalSeconds;

    private MissCacheHandler<T> missCacheHandler;

    private Policy policy;


    /**
     * 过期缓存策略
     * @param expireSeconds 缓存过期时间
     * @param handler   数据源获取的处理器
     */
    public CachePolicy(int expireSeconds, MissCacheHandler<T> handler){
        this.expireSeconds = expireSeconds;
        this.missCacheHandler = handler;
        this.policy = Policy.Expired;
    }

    /**
     * 定时刷新缓存策略
     * @param delaySeconds  一开始延迟执行的时间
     * @param intervalSeconds   间隔刷新时间
     * @param handler   数据源获取的处理器
     */
    public CachePolicy(int delaySeconds, int intervalSeconds, MissCacheHandler<T> handler){
        this.delaySeconds = delaySeconds;
        this.intervalSeconds = intervalSeconds;
        this.missCacheHandler = handler;
        this.policy = Policy.Timing;
    }

    public int getExpireSeconds() {
        return expireSeconds;
    }

    public int getIntervalSeconds() {
        return intervalSeconds;
    }

    public MissCacheHandler<T> getMissCacheHandler() {
        return missCacheHandler;
    }

    public int getDelaySeconds() {
        return delaySeconds;
    }

    public Policy getPolicy() {
        return policy;
    }

    public boolean isExpired(){
        return (Policy.Expired == policy);
    }

    public boolean isTiming(){
        return (Policy.Timing == policy);
    }
}
