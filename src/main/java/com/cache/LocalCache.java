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
        System.out.println("cache set");
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
        if(localValue != null && localValue.expire > currentTimeMillis){
            result = localValue.value;
        }else if(localValue == null || localValue.expire <= currentTimeMillis){
            CachePloy<T> cachePloy = cachePloyRegister.get(key);
            if(cachePloy != null){
                MissCacheHandler<T> handler = cachePloy.getMissCacheHandler();
                result = set(key, handler.getData(), cachePloy.getExpireSeconds());
            }
        }
        System.out.println("cache get result: " + result);
        return result;
    }

    @Override
    public <T> T get(String key, int expireSeconds, Class<T> clazz, MissCacheHandler<T> handler) {

        long currentTimeMillis = System.currentTimeMillis();
        LocalValue<T> localValue = caches.get(key);
        T result = null;
        if(localValue != null && localValue.expire >= currentTimeMillis){
            result = localValue.value;
        }else if(localValue == null || localValue.expire < currentTimeMillis){
            result = set(key, handler.getData(), expireSeconds);
        }
        return result;
    }

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
