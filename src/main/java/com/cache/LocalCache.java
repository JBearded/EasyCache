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

    private ConcurrentMap<String, CachePloy<String>> cachePloyRegister = new ConcurrentHashMap<>();

    private ConcurrentMap<String, CachePloy<String>> missCachePloyRegister = new ConcurrentHashMap<>();

//    private boolean isOpen

    private Lock registerLock = new ReentrantLock();

    private int DEFAULT_EXPIRE_SECONDS = 60 * 60 * 24;

    /**
     * 注册缓存key, 设置过期缓存或者定时刷新缓存
     * @param key 缓存key
     * @param cachePloy 缓存策略
     */
    public void register(String key, CachePloy<String> cachePloy){

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

    private boolean checkPloyAvailable(CachePloy cachePloy){

        return true;
    }

    private void initPloy(String key, CachePloy cachePloy){
        cachePloyRegister.put(key, cachePloy);
        if(cachePloy.getIntervalSeconds() > 0){
            initTimerCache(key, cachePloy);
        }else{
            initExpireCache(key, cachePloy);
        }
    }

    private void initTimerCache(final String key, final CachePloy cachePloy){
        scheduler.run(key, cachePloy.getDelaySeconds(), cachePloy.getIntervalSeconds(), new Runnable() {
            @Override
            public void run() {
                MissCacheHandler<String> handler = cachePloy.getMissCacheHandler();
                String value = handler.getData();
                set(key, value);
            }
        });
    }

    private void initExpireCache(String key, CachePloy cachePloy){
        MissCacheHandler<String> handler = cachePloy.getMissCacheHandler();
        String value = handler.getData();
        set(key, value, cachePloy.getExpireSeconds());
    }

    private void set(String key, String value){
        this.set(key, value, DEFAULT_EXPIRE_SECONDS);
    }

    private void set(String key, String value, int expireSeconds){
        LocalValue localValue = new LocalValue();
        localValue.value = value;
        localValue.expire = System.currentTimeMillis() + expireSeconds * 1000;
        caches.put(key, localValue);
    }

    /**
     * 获取注册过的缓存
     * @param key 注册过的缓存key
     * @return
     */
    public String get(String key){
        long currentTimeMillis = System.currentTimeMillis();
        LocalValue localValue = caches.get(key);
        long expire = localValue.expire;
        if(expire >= currentTimeMillis){
            return localValue.value;
        }else{
            CachePloy cachePloy = cachePloyRegister.get(key);
            initExpireCache(key, cachePloy);
            return caches.get(key).value;
        }
    }

    class LocalValue{
        String value;
        long expire;
    }
}
