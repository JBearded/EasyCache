package com.ecache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.SoftReference;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 本地缓存
 * @author 谢俊权
 * @create  
 */
public class LocalCache extends AbstractCache{

    private static final Logger logger = LoggerFactory.getLogger(LocalCache.class);

    private ConcurrentMap<String, SoftLocalValue> caches = new ConcurrentHashMap<>();

    public LocalCache() {
        super();
        clearScheduler();
    }

    public LocalCache(CacheConfig config){
        super(config);
        clearScheduler();
    }

    @Override
    public <T> T set(String key, T value, int expireSeconds){
        if(key == null || value == null){
            throw new NullPointerException();
        }
        hashLock.lock(key);
        try{
            SoftLocalValue<T> localValue = new SoftLocalValue<>(value, expireSeconds);
            caches.put(key, localValue);
            logger.info("local cache set key:{} value:{}", key, value);
        }finally {
            hashLock.unlock(key);
        }
        return value;
    }

    @Override
    public <T> T get(String key, int expiredSeconds, Class<T> clazz, MissCacheHandler<T> handler) {

        hashLock.lock(key);
        T result = null;
        try{
            SoftLocalValue<T> localValue = caches.get(key);
            result = (localValue == null || localValue.get() == null || localValue.get().isExpired()) ? null : (T) localValue.get().value;
            if(result == null){
                logger.info("local cache get key:{} null and reload", key);
                caches.remove(key);
                if(handler != null){
                    result = set(key, handler.getData(), expiredSeconds);
                }
            }
        }finally {
            hashLock.unlock(key);
        }
        logger.info("local cache get key:{} value:{}", key, result);
        return result;
    }

    /**
     * 启动定时清除本地缓存数据
     */
    private void clearScheduler(){
        int delaySeconds = 60;
        int intervalSeconds = cacheConfig.getClearSchedulerIntervalSeconds();
        scheduler.run("clear-caches", delaySeconds, intervalSeconds, new Runnable() {
            @Override
            public void run() {
                Iterator<String> keyIt = caches.keySet().iterator();
                while(keyIt.hasNext()){
                    String key = keyIt.next();
                    hashLock.lock(key);
                    try{
                        SoftLocalValue<Object> localValue = caches.get(key);
                        if(localValue != null && localValue.get() != null && localValue.get().isExpired()){
                            keyIt.remove();
                        }
                    }finally {
                        hashLock.unlock(key);
                    }
                }
            }
        });
    }

    class LocalValue<T>{
        T value;
        long expiredMS;

        public LocalValue(T value, int expiredSeconds) {
            this.value = value;
            this.expiredMS = System.currentTimeMillis() + expiredSeconds * 1000L;
        }

        private boolean isExpired(){
            long currentTimeMillis = System.currentTimeMillis();
            return expiredMS < currentTimeMillis;
        }
    }

    class SoftLocalValue<T> extends SoftReference<LocalValue> {

        public SoftLocalValue(T value, int expiredSeconds) {
            super(new LocalValue(value, expiredSeconds));
        }
    }
}
