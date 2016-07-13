package com.cache;

import com.alibaba.fastjson.JSON;

/**
 * 远程缓存
 * @author 谢俊权
 * @create 2016/7/9 16:31
 */
public class RemoteCache extends AbstractCache{

    private RemoteCacheInterface remoteCacheInterface;

    public RemoteCache(RemoteCacheInterface remoteCacheInterface) {
        super();
        this.remoteCacheInterface = remoteCacheInterface;
    }

    public RemoteCache(CacheConfig config, RemoteCacheInterface remoteCacheInterface) {
        super(config);
        this.remoteCacheInterface = remoteCacheInterface;
    }

    @Override
    protected <T> T set(String key, T value, int expireSeconds) {
        String json =JSON.toJSONString(value);
        remoteCacheInterface.set(key, json, expireSeconds);
        return value;
    }

    @Override
    public <T> T get(String key, Class<T> clazz) {
        String result = remoteCacheInterface.get(key);
        if(result == null){
            CachePloy<T> cachePloy = cachePloyRegister.get(key);
            return initExpireCache(key, cachePloy);
        }
        return JSON.parseObject(result, clazz);
    }

    @Override
    public <T> T get(String key, int expireSeconds, Class<T> clazz, MissCacheHandler<T> handler) {
        String result = remoteCacheInterface.get(key);
        if(result == null){
            return set(key, handler.getData(), expireSeconds);
        }
        return JSON.parseObject(result, clazz);
    }
}