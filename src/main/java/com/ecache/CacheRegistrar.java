package com.ecache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 公用缓存工具
 * @author 谢俊权
 * @create 2016/7/12 13:49
 */
public abstract class CacheRegistrar extends AbstractEasyCache {

    private static final Logger logger = LoggerFactory.getLogger(CacheRegistrar.class);

    /**
     * 定时器, 用于定时刷新缓存
     */
    protected Scheduler scheduler;

    /**
     * 缓存服务的注册数据
     */
    protected ConcurrentMap<String, CachePolicy> cachePolicyRegister = new ConcurrentHashMap<>();

    public CacheRegistrar(CacheConfig config) {
        super(config);
        this.scheduler = new Scheduler(this.cacheConfig.getSchedulerCorePoolSize());
    }

    /**
     * 注册缓存key, 设置过期缓存或者定时刷新缓存
     * @param key 缓存key
     * @param cachePolicy 缓存策略
     */
    public <T> void register(String key, CachePolicy<T> cachePolicy){

        hashLock.lock(key);
        try{
            if(cachePolicyRegister.containsKey(key)){
                logger.info("cache contains key register {} and remove", key);
                CachePolicy oldCachePolicy = cachePolicyRegister.remove(key);
                if(oldCachePolicy.isTiming()){
                    scheduler.cancel(key);
                }
            }
            initPolicy(key, cachePolicy);
        }finally {
            hashLock.unlock(key);
        }
    }

    /**
     * 在注册缓存成功后, 马上获取对应的数据并缓存
     * @param key   缓存key
     * @param cachePolicy   缓存策略
     * @param <T>
     */
    private  <T> void initPolicy(String key, CachePolicy<T> cachePolicy){
        cachePolicyRegister.put(key, cachePolicy);
        if(cachePolicy.isTiming()){
            logger.info("init cache timing policy, key:{}", key);
            initIntervalCache(key, cachePolicy);
        }else if(cachePolicy.isExpired()){
            logger.info("init cache expired policy, key:{}", key);
            initExpiredCache(key, cachePolicy);
        }
    }

    private <T> void initIntervalCache(String key, CachePolicy<T> cachePolicy){
        scheduler.run(
                key,
                cachePolicy.getDelaySeconds(),
                cachePolicy.getIntervalSeconds(),
                new IntervalCacheTask(key, cachePolicy.getExpiredSeconds(), cachePolicy.getMissCacheHandler()));
    }

    private class IntervalCacheTask<T> implements Runnable{

        private String key;
        private int expiredSeconds;
        private MissCacheHandler<T> handler;

        public IntervalCacheTask(String key, int expiredSeconds, MissCacheHandler<T> handler) {
            this.key = key;
            this.expiredSeconds = expiredSeconds;
            this.handler = handler;
        }

        @Override
        public void run() {
            T value = handler.getData();
            set(key, value, expiredSeconds);
        }
    }

    private <T> T initExpiredCache(String key, CachePolicy<T> cachePolicy){
        MissCacheHandler<T> handler = cachePolicy.getMissCacheHandler();
        T value = handler.getData();
        return (cachePolicy.getExpiredSeconds() <= 0)
                ? set(key, value, cacheConfig.getDefaultExpiredSeconds())
                : set(key, value, cachePolicy.getExpiredSeconds());
    }

    /**
     * 获取缓存中的数据
     * @param key 缓存key
     * @param type 缓存数据类型(支持泛型)
     * @param <T>
     * @return
     */
    @Override
    public <T> T get(String key, CacheType<T> type){
        CachePolicy<T> cachePolicy = cachePolicyRegister.get(key);
        MissCacheHandler<T> handler = (cachePolicy == null) ? null : cachePolicy.getMissCacheHandler();
        int expiredSeconds = (cachePolicy == null)
                ? cacheConfig.getDefaultExpiredSeconds()
                : cachePolicy.getExpiredSeconds();
        return get(key, expiredSeconds, type, handler);
    }
}
