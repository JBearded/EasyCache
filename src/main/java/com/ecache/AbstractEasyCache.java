package com.ecache;

import com.alibaba.fastjson.JSON;

/**
 * @author xiejunquan
 * @create 2016/12/1 16:00
 */
public abstract class AbstractEasyCache extends AbstractCacheRegistrar implements EasyCacheSource {

    public AbstractEasyCache(CacheConfig cacheConfig) {
        super(cacheConfig);
    }

    @Override
    public <T> T set(String key, T value, int expiredSeconds) {
        String json = JSON.toJSONString(value);
        setString(key, json, expiredSeconds);
        return value;
    }

    @Override
    public <T> T get(String key, Class<T> clazz) {
        return get(key, new CacheType<T>(clazz){});
    }

    @Override
    public <T> T get(String key, int expiredSeconds, Class<T> clazz, MissCacheHandler<T> handler) {
        return get(key, expiredSeconds, new CacheType<T>(clazz){}, handler);
    }

    @Override
    public <T> T get(String key, CacheType<T> type) {
        return get(key, 0, type, null);
    }

    @Override
    public <T> T get(String key, int expiredSeconds, CacheType<T> type, MissCacheHandler<T> handler) {
        String result = getString(key);
        if(result == null && handler != null){
            if(!cacheConfig.isAvoidServerOverload()){
                return set(key, handler.getData(), expiredSeconds);
            }
            hashLock.lock(key);
            try{
                result = getString(key);
                if(result == null){
                    return set(key, handler.getData(), expiredSeconds);
                }
            }finally {
                hashLock.unlock(key);
            }
        }
        return (type.actualType instanceof Class)
                ? (T) JSON.parseObject(result, (Class)type.actualType)
                : (T) JSON.parseObject(result, type.actualType);
    }

    public CacheConfig getCacheConfig() {
        return cacheConfig;
    }
}
