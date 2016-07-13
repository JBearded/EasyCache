package com.cache;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 公用缓存工具
 * @author 谢俊权
 * @create 2016/7/12 13:49
 */
public abstract class AbstractCache {

    protected Scheduler scheduler;

    protected CacheConfig config;

    protected ConcurrentMap<String, CachePloy> cachePloyRegister = new ConcurrentHashMap<>();

    private final Lock registerLock = new ReentrantLock();

    public AbstractCache() {
        this(new CacheConfig());
    }

    public AbstractCache(CacheConfig config) {
        this.config = config;
        this.scheduler = new Scheduler(this.config.getSchedulerCorePoolSize());
    }

    /**
     * 注册缓存key, 设置过期缓存或者定时刷新缓存
     * @param key 缓存key
     * @param cachePloy 缓存策略
     */
    public <T> void register(String key, CachePloy<T> cachePloy){

        if(registerLock.tryLock()){
            try{
                if(cachePloyRegister.containsKey(key)){
                    CachePloy oldCachePloy = cachePloyRegister.remove(key);
                    if(oldCachePloy.getIntervalSeconds() > 0){
                        scheduler.cancel(key);
                    }
                }
                initPloy(key, cachePloy);
            }finally {
                registerLock.unlock();
            }
        }else{
            retryRegister(key, cachePloy);
        }
    }

    protected  <T> void retryRegister(final String key, final CachePloy<T> cachePloy){
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                register(key, cachePloy);
            }
        }, this.config.getRetryRegisterDelayMillisSecond());
    }

    protected  <T> void initPloy(String key, CachePloy<T> cachePloy){
        cachePloyRegister.put(key, cachePloy);
        if(cachePloy.getIntervalSeconds() > 0){
            initIntervalCache(key, cachePloy);
        }else{
            initExpireCache(key, cachePloy);
        }
    }

    protected <T> void initIntervalCache(final String key, final CachePloy<T> cachePloy){
        scheduler.run(key, cachePloy.getDelaySeconds(), cachePloy.getIntervalSeconds(), new Runnable() {
            @Override
            public void run() {
                MissCacheHandler<T> handler = cachePloy.getMissCacheHandler();
                T value = handler.getData();
                set(key, value, config.getDefaultExpireSeconds());
            }
        });
    }

    protected <T> T initExpireCache(String key, CachePloy<T> cachePloy){
        MissCacheHandler<T> handler = cachePloy.getMissCacheHandler();
        T value = handler.getData();
        if(cachePloy.getExpireSeconds() <= 0){
            return set(key, value, this.config.getDefaultExpireSeconds());
        }else{
            return set(key, value, cachePloy.getExpireSeconds());
        }
    }

    protected abstract  <T> T set(String key, T value, int expireSeconds);

    /**
     * 获取注册过的缓存, get(key, T.class)调用
     * @param key 注册过的缓存key
     * @return
     */
    protected abstract  <T> T get(String key, Class<T> clazz);

    protected abstract <T> T get(String key, int expireSeconds, Class<T> clazz, MissCacheHandler<T> handler);
}
