package com.cache;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author 谢俊权
 * @create  
 */
public class LocalCache{

    private Scheduler scheduler;

    private CacheConfig config;

    private ConcurrentMap<String, LocalValue> caches = new ConcurrentHashMap<>();

    private ConcurrentMap<String, CachePloy> cachePloyRegister = new ConcurrentHashMap<>();

    private Lock registerLock = new ReentrantLock();

    public LocalCache() {
        this.config = new CacheConfig();
        this.scheduler = new Scheduler(this.config.getSchedulerCorePoolSize());
    }

    public LocalCache(CacheConfig config){
        this.config = config;
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

    private <T> void retryRegister(final String key, final CachePloy<T> cachePloy){
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                register(key, cachePloy);
            }
        }, this.config.getRetryRegisterDelayMillisSecond());
    }

    private <T> void initPloy(String key, CachePloy<T> cachePloy){
        cachePloyRegister.put(key, cachePloy);
        if(cachePloy.getIntervalSeconds() > 0){
            initIntervalCache(key, cachePloy);
        }else{
            initExpireCache(key, cachePloy);
        }
    }

    private <T> void initIntervalCache(final String key, final CachePloy<T> cachePloy){
        scheduler.run(key, cachePloy.getDelaySeconds(), cachePloy.getIntervalSeconds(), new Runnable() {
            @Override
            public void run() {
                MissCacheHandler<T> handler = cachePloy.getMissCacheHandler();
                T value = handler.getData();
                set(key, value);
            }
        });
    }

    private <T> void initExpireCache(String key, CachePloy<T> cachePloy){
        MissCacheHandler<T> handler = cachePloy.getMissCacheHandler();
        T value = handler.getData();
        if(cachePloy.getExpireSeconds() <= 0){
            set(key, value);
        }else{
            set(key, value, cachePloy.getExpireSeconds());
        }
    }

    private <T> void set(String key, T value){
        this.set(key, value, this.config.getDefaultExpireSeconds());
    }

    private <T> void set(String key, T value, int expireSeconds){
        LocalValue<T> localValue = new LocalValue<T>();
        localValue.value = value;
        localValue.expire = System.currentTimeMillis() + expireSeconds * 1000;
        caches.put(key, localValue);
    }

    /**
     * 获取注册过的缓存, get(key, T.class)调用
     * @param key 注册过的缓存key
     * @return
     */
    public <T> T get(String key, Class<T> clazz){
        return get(key);
    }

    /**
     * 获取注册过的缓存, <T>get(key)调用
     * @param key 注册过的缓存key
     * @return
     */
    public <T> T get(String key){
        long currentTimeMillis = System.currentTimeMillis();
        LocalValue<T> localValue = caches.get(key);
        T result = null;
        if(localValue != null){
            long expire = localValue.expire;
            if(expire >= currentTimeMillis){
                result = localValue.value;
            }else{
                CachePloy<T> cachePloy = cachePloyRegister.get(key);
                initExpireCache(key, cachePloy);
                localValue = caches.get(key);
                result = localValue.value;
            }
        }
        return result;
    }


    class LocalValue<T>{
        T value;
        long expire;
    }
}
