package com.ecache;

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
        lock(key);
        try{
            LocalValue<T> localValue = new LocalValue<>();
            localValue.value = value;
            localValue.expire = System.currentTimeMillis() + expireSeconds * 1000;
            caches.put(key, localValue);
        }finally {
            unlock(key);
        }
        return value;
    }

    @Override
    public <T> T get(String key, int expiredSeconds, Class<T> clazz, MissCacheHandler<T> handler) {

        lock(key);
        T result = null;
        try{
            LocalValue<T> localValue = caches.get(key);
            long currentTimeMillis = System.currentTimeMillis();
            result = (localValue == null || localValue.expire < currentTimeMillis) ? null : localValue.value;
            if(result == null){
                caches.remove(key);
                if(handler != null){
                    result = set(key, handler.getData(), expiredSeconds);
                }
            }
        }finally {
            unlock(key);
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
