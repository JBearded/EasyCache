package com.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author 谢俊权
 * @create 2016/7/9 16:31
 */
public class CacheService {

    private ConcurrentMap<String, CachePloy> cachePloyMap = new ConcurrentHashMap<>();

    private LocalCache localCache;

    private RemoteCache remoteCache;

    public CacheService(){

    }

    public <T> T getLocalCache(String key){

        return null;
    }

    public <T> T getRemoteCache(String key){

        return null;

    }


}
