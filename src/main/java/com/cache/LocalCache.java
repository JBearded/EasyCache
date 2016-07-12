package com.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author 谢俊权
 * @create  
 */
public class LocalCache{

    private Scheduler scheduler = new Scheduler(64);

    private ConcurrentMap<String, LocalValue> caches = new ConcurrentHashMap<>();

    private ConcurrentMap<String, CachePloy> cachePloyRegister = new ConcurrentHashMap<>();

    private ConcurrentMap<String, CachePloy> missCachePloyRegister = new ConcurrentHashMap<>();

    private Lock registerLock = new ReentrantLock();

    private int DEFAULT_EXPIRE_SECONDS = 60 * 60 * 24;

    /**
     * 注册缓存key, 设置过期缓存或者定时刷新缓存
     * @param key 缓存key
     * @param cachePloy 缓存策略
     */
    public <T> void register(String key, CachePloy<T> cachePloy){

        if(!checkPloyAvailable(cachePloy) || key == null || "".equals(key.trim())){
            return;
        }
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
            missCachePloyRegister.put(key, cachePloy);
        }
    }

    private <T> boolean checkPloyAvailable(CachePloy<T> cachePloy){

        return true;
    }

    private <T> void initPloy(String key, CachePloy<T> cachePloy){
        cachePloyRegister.put(key, cachePloy);
        if(cachePloy.getIntervalSeconds() > 0){
            initTimerCache(key, cachePloy);
        }else{
            initExpireCache(key, cachePloy);
        }
    }

    private <T> void initTimerCache(final String key, final CachePloy<T> cachePloy){
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
        set(key, value, cachePloy.getExpireSeconds());
    }

    private <T> void set(String key, T value){
        this.set(key, value, DEFAULT_EXPIRE_SECONDS);
    }

    private <T> void set(String key, T value, int expireSeconds){
        LocalValue<T> localValue = new LocalValue<T>();
        localValue.value = value;
        localValue.expire = System.currentTimeMillis() + expireSeconds * 1000;
        caches.put(key, localValue);
    }

    /**
     * 获取注册过的缓存
     * @param key 注册过的缓存key
     * @return
     */
    public <T> T get(String key, Class<T> clazz){
        long currentTimeMillis = System.currentTimeMillis();
        LocalValue<T> localValue = caches.get(key);
        long expire = localValue.expire;
        if(expire >= currentTimeMillis){
            return localValue.value;
        }else{
            CachePloy<T> cachePloy = cachePloyRegister.get(key);
            initExpireCache(key, cachePloy);
            localValue = caches.get(key);
            return localValue.value;
        }
    }

    class LocalValue<T>{
        T value;
        long expire;
    }
}
