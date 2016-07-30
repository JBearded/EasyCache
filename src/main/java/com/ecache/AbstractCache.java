package com.ecache;

import com.ecache.utils.HashLock;

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

    /**
     * 定时器, 用于定时刷新缓存
     */
    protected Scheduler scheduler;

    /**
     * 分段锁
     */
    protected HashLock hashLock;

    /**
     * 缓存服务相关的配置信息
     */
    protected CacheConfig cacheConfig;

    /**
     * 缓存服务的注册数据
     */
    protected ConcurrentMap<String, CachePolicy> cachePolicyRegister = new ConcurrentHashMap<>();

    private final Lock registerLock = new ReentrantLock();

    public AbstractCache() {
        this(new CacheConfig.Builder().build());
    }

    public AbstractCache(CacheConfig config) {
        this.cacheConfig = config;
        this.scheduler = new Scheduler(this.cacheConfig.getSchedulerCorePoolSize());
        this.hashLock = new HashLock(this.cacheConfig.getLockSegments(), this.cacheConfig.isLockIsFair());
    }

    /**
     * 注册缓存key, 设置过期缓存或者定时刷新缓存
     * @param key 缓存key
     * @param cachePolicy 缓存策略
     */
    public <T> void register(String key, CachePolicy<T> cachePolicy){

        if(registerLock.tryLock()){
            try{
                if(cachePolicyRegister.containsKey(key)){
                    CachePolicy oldCachePolicy = cachePolicyRegister.remove(key);
                    if(oldCachePolicy.isTiming()){
                        scheduler.cancel(key);
                    }
                }
                initPolicy(key, cachePolicy);
            }finally {
                registerLock.unlock();
            }
        }else{
            retryRegister(key, cachePolicy);
        }
    }

    /**
     * 重新注册缓存key
     * @param key 缓存key
     * @param cachePolicy 缓存策略
     * @param <T>
     */
    protected  <T> void retryRegister(final String key, final CachePolicy<T> cachePolicy){
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                register(key, cachePolicy);
            }
        }, cacheConfig.getRetryRegisterMSeconds());
    }

    /**
     * 在注册缓存成功后, 马上获取对应的数据并缓存
     * @param key   缓存key
     * @param cachePolicy   缓存策略
     * @param <T>
     */
    protected  <T> void initPolicy(String key, CachePolicy<T> cachePolicy){
        cachePolicyRegister.put(key, cachePolicy);
        if(cachePolicy.isTiming()){
            initIntervalCache(key, cachePolicy);
        }else if(cachePolicy.isExpired()){
            initExpiredCache(key, cachePolicy);
        }
    }

    protected <T> void initIntervalCache(final String key, final CachePolicy<T> cachePolicy){
        scheduler.run(key, cachePolicy.getDelaySeconds(), cachePolicy.getIntervalSeconds(), new Runnable() {
            @Override
            public void run() {
                MissCacheHandler<T> handler = cachePolicy.getMissCacheHandler();
                T value = handler.getData();
                set(key, value, cacheConfig.getDefaultExpiredSeconds());
            }
        });
    }

    protected <T> T initExpiredCache(String key, CachePolicy<T> cachePolicy){
        MissCacheHandler<T> handler = cachePolicy.getMissCacheHandler();
        T value = handler.getData();
        if(cachePolicy.getExpireSeconds() <= 0){
            return set(key, value, this.cacheConfig.getDefaultExpiredSeconds());
        }else{
            return set(key, value, cachePolicy.getExpireSeconds());
        }
    }

    public void lock(Object key){
        hashLock.lock(key);
    }

    public void unlock(Object key){
        hashLock.unlock(key);
    }

    /**
     * 保存数据到缓存中
     * @param key 缓存key
     * @param value 缓存数据
     * @param expiredSeconds 缓存过期时间
     * @param <T>
     * @return
     */
    public abstract  <T> T set(String key, T value, int expiredSeconds);

    /**
     * 获取缓存中的数据
     * @param key 缓存key
     * @param clazz 缓存数据类型
     * @param <T>
     * @return
     */
    public <T> T get(String key, Class<T> clazz){
        CachePolicy<T> cachePolicy = cachePolicyRegister.get(key);
        MissCacheHandler<T> handler = (cachePolicy == null) ? null : cachePolicy.getMissCacheHandler();
        int expiredSeconds = (cachePolicy == null) ? cacheConfig.getDefaultExpiredSeconds() : cachePolicy.getExpireSeconds();
        return get(key, expiredSeconds, clazz, handler);
    }

    /**
     * 获取缓存中的数据, 如果没有key对应的数据, 则从handler中获取并存入缓存中
     * @param key   缓存key
     * @param expireSeconds 缓存过期时间
     * @param clazz 缓存数据类型
     * @param handler   数据源获取类
     * @param <T>
     * @return
     */
    public abstract <T> T get(String key, int expireSeconds, Class<T> clazz, MissCacheHandler<T> handler);
}
