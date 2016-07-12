package com.cache;

/**
 * @author 谢俊权
 * @create 2016/7/9 17:53
 */
public class CachePloy<T> {

    private int expireSeconds;

    private int delaySeconds;

    private int intervalSeconds;

    private MissCacheHandler<T> missCacheHandler;

    /**
     * 过期缓存策略
     * @param expireSeconds 缓存过期时间
     * @param handler   数据源获取的处理器
     */
    public CachePloy(int expireSeconds, MissCacheHandler<T> handler){
        this.expireSeconds = expireSeconds;
        this.missCacheHandler = handler;
    }

    /**
     * 定时刷新缓存策略
     * @param delaySeconds  一开始延迟执行的时间
     * @param intervalSeconds   间隔刷新时间
     * @param handler   数据源获取的处理器
     */
    public CachePloy(int delaySeconds, int intervalSeconds, MissCacheHandler<T> handler){
        this.delaySeconds = delaySeconds;
        this.intervalSeconds = intervalSeconds;
        this.missCacheHandler = handler;
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
}
