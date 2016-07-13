package com.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 本地缓存
 * @author 谢俊权
 * @create  
 */
public class LocalCache extends AbstractCache{

    private Map<String, LocalValue> caches = new HashMap<>();

    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    public LocalCache() {
        super();
    }

    public LocalCache(CacheConfig config){
        super(config);
    }

    @Override
    protected <T> T set(String key, T value, int expireSeconds){
        LocalValue<T> localValue = new LocalValue<>();
        localValue.value = value;
        localValue.expire = System.currentTimeMillis() + expireSeconds * 1000;
        caches.put(key, localValue);
        return value;
    }

    @Override
    public <T> T get(String key, Class<T> clazz){
        rwLock.readLock().lock();
        long currentTimeMillis = System.currentTimeMillis();
        LocalValue<T> localValue = caches.get(key);
        rwLock.readLock().unlock();
        rwLock.writeLock().lock();
        T result = null;
        try{
            if(localValue != null && localValue.expire > currentTimeMillis){
                result = localValue.value;
            }else if(localValue == null || localValue.expire <= currentTimeMillis){
                CachePloy<T> cachePloy = cachePloyRegister.get(key);
                MissCacheHandler<T> handler = cachePloy.getMissCacheHandler();
                result = set(key, handler.getData(), cachePloy.getExpireSeconds());
            }
        }finally {
            rwLock.writeLock().unlock();
        }
        return result;
    }

    @Override
    public <T> T get(String key, int expireSeconds, Class<T> clazz, MissCacheHandler<T> handler) {
        rwLock.readLock().lock();
        long currentTimeMillis = System.currentTimeMillis();
        LocalValue<T> localValue = caches.get(key);
        rwLock.readLock().unlock();
        rwLock.writeLock().lock();
        T result = null;
        try{
            if(localValue != null && localValue.expire >= currentTimeMillis){
                result = localValue.value;
            }else if(localValue == null || localValue.expire < currentTimeMillis){
                result = set(key, handler.getData(), expireSeconds);
            }
        }finally {
            rwLock.writeLock().unlock();
        }
        return result;
    }

    class LocalValue<T>{
        T value;
        long expire;
    }
}
