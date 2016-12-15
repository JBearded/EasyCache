package com.ecache;

import com.ecache.utils.HashLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 缓存注册器
 * @author xiejunquan
 * @create 2016/12/1 19:07
 */
public abstract class AbstractCacheRegistrar implements EasyCache{

    private static final Logger logger = LoggerFactory.getLogger(AbstractCacheRegistrar.class);

    /**
     * 定时器, 用于定时刷新缓存
     */
    protected Scheduler scheduler;

    /**
     * 缓存服务相关的配置信息
     */
    protected CacheConfig cacheConfig;

    /**
     * 哈希锁, 每个key都有对应的一个锁
     */
    protected HashLock hashLock;

    /**
     * 缓存服务的注册数据
     */
    protected ConcurrentMap<String, CachePolicy> cachePolicyRegister = new ConcurrentHashMap<>();

    public AbstractCacheRegistrar(CacheConfig cacheConfig) {
        this.cacheConfig = (cacheConfig == null) ? new CacheConfig.Builder().build() : cacheConfig;
        this.hashLock = new HashLock(cacheConfig.getLockSegments(), this.cacheConfig.isLockIsFair());
        this.scheduler = new Scheduler(cacheConfig.getSchedulerCorePoolSize());
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
                logger.info("EasyCache contains key register:{} and remove", key);
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
            logger.info("register timing cache policy and init, key:{}", key);
            initIntervalCache(key, cachePolicy);
        }else if(cachePolicy.isExpired()){
            logger.info("init expired cache policy and init, key:{}", key);
            initExpiredCache(key, cachePolicy);
        }
    }

    /**
     * 初始化定时缓存
     * @param key   缓存key
     * @param cachePolicy   缓存策略
     * @param <T>   返回值类型
     */
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

    /**
     * 初始化过期缓存
     * @param key   缓存key
     * @param cachePolicy   缓存策略
     * @param <T>   返回类型
     * @return
     */
    private <T> T initExpiredCache(String key, CachePolicy<T> cachePolicy){
        MissCacheHandler<T> handler = cachePolicy.getMissCacheHandler();
        T value = handler.getData();
        int expired = (cachePolicy.getExpiredSeconds() <= 0) ? cacheConfig.getDefaultExpiredSeconds() : cachePolicy.getExpiredSeconds();
        return set(key, value, expired);
    }
}
