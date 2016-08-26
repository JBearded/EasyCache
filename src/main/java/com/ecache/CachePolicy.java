package com.ecache;

/**
 * 缓存策略
 * @author 谢俊权
 * @create 2016/7/9 17:53
 */
public class CachePolicy<T> {

    /**
     * 缓存过期时间, 用于过期缓存策略
     */
    private int expiredSeconds;

    /**
     * 延迟时间, 用于定时刷新缓存策略
     */
    private int delaySeconds;

    /**
     * 间隔时间, 用于定时刷新缓存策略
     */
    private int intervalSeconds;

    /**
     * 数据源获取处理器
     */
    private MissCacheHandler<T> missCacheHandler;

    /**
     * 缓存的策略, Timing或者Expired
     */
    private Policy policy;


    /**
     * 过期缓存策略
     * @param expiredSeconds 缓存过期时间
     * @param handler   数据源获取的处理器
     */
    public CachePolicy(int expiredSeconds, MissCacheHandler<T> handler){
        this.expiredSeconds = expiredSeconds;
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

    public int getExpiredSeconds() {
        return expiredSeconds;
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

    public boolean isExpired(){
        return (Policy.Expired == policy);
    }

    public boolean isTiming(){
        return (Policy.Timing == policy);
    }
}
