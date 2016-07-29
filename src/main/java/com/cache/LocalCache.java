package com.cache;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 本地缓存
 * @author 谢俊权
 * @create  
 */
public class LocalCache extends AbstractCache{

    private ConcurrentMap<String, LocalValue> caches = new ConcurrentHashMap<>();

    public LocalCache() {
        super();
    }

    public LocalCache(CacheConfig config){
        super(config);
    }

    @Override
    public <T> T set(String key, T value, int expireSeconds){
        System.out.println("cache set key: " + key + " value: "+value);
        LocalValue<T> localValue = new LocalValue<>();
        localValue.value = value;
        localValue.expire = System.currentTimeMillis() + expireSeconds * 1000;
        caches.put(key, localValue);
        return value;
    }

    @Override
    public <T> T get(String key, Class<T> clazz){

        long currentTimeMillis = System.currentTimeMillis();
        LocalValue<T> localValue = caches.get(key);
        T result = null;
        if(localValue == null || localValue.expire <= currentTimeMillis){
            CachePolicy<T> cachePolicy = cachePolicyRegister.get(key);
            if(cachePolicy != null){
                MissCacheHandler<T> handler = cachePolicy.getMissCacheHandler();
                result = set(key, handler.getData(), cachePolicy.getExpireSeconds());
            }
        }else if(localValue != null && localValue.expire > currentTimeMillis){
            result = localValue.value;
        }
        System.out.println("cache get key: " + key + " result: " + result);
        return result;
    }

    @Override
    public <T> T get(String key, int expireSeconds, Class<T> clazz, MissCacheHandler<T> handler) {

        long currentTimeMillis = System.currentTimeMillis();
        LocalValue<T> localValue = caches.get(key);
        T result = null;
        if(localValue == null || localValue.expire < currentTimeMillis){
            result = set(key, handler.getData(), expireSeconds);
        }else if(localValue != null && localValue.expire >= currentTimeMillis){
            result = localValue.value;
        }
        return result;
    }

    /**
     * 启动定时清除本地缓存数据, 每天零点启动清除
     */
    public void clearScheduler(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.DATE, 1);
        int delay = (int) ((calendar.getTimeInMillis() - System.currentTimeMillis()) / 1000);
        int interval = 60 * 60 * 24;
        scheduler.run("clear-caches", delay, interval, new Runnable() {
            @Override
            public void run() {
                caches.clear();
            }
        });
    }

    class LocalValue<T>{
        T value;
        long expire;
    }
}
