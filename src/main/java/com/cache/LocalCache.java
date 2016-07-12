package com.cache;

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
    public  <T> T set(String key, T value){
        this.set(key, value, this.config.getDefaultExpireSeconds());
        return value;
    }

    @Override
    public <T> T set(String key, T value, int expireSeconds){
        LocalValue<T> localValue = new LocalValue<>();
        localValue.value = value;
        localValue.expire = System.currentTimeMillis() + expireSeconds * 1000;
        caches.put(key, localValue);
        return value;
    }

    @Override
    public <T> T get(String key, Class<T> clazz){
        return get(key);
    }

    @Override
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
                result = initExpireCache(key, cachePloy);
            }
        }
        return result;
    }


    class LocalValue<T>{
        T value;
        long expire;
    }
}
