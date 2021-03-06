package com.ecache;

/**
 * @author xiejunquan
 * @create 2016/12/1 15:26
 */
public interface EasyCache {


    /**
     * 注册缓存策略到EasyCache中
     * @param key   缓存key
     * @param cachePolicy   缓存策略(定时或过期)
     * @param <T>
     */
    <T> void register(String key, CachePolicy<T> cachePolicy);

    /**
     * 保存数据到缓存中
     * @param key 缓存key
     * @param value 缓存数据
     * @param expiredSeconds 缓存过期时间
     * @param <T>
     * @return
     */
    <T> T set(String key, T value, int expiredSeconds);


    /**
     * 获取缓存中的数据
     * @param key 缓存key
     * @param clazz 缓存数据类型
     * @param <T>
     * @return
     */
    <T> T get(String key, Class<T> clazz);

    /**
     * 获取缓存中的数据, 如果没有key对应的数据, 则从handler中获取并存入缓存中
     * @param key   缓存key
     * @param expiredSeconds 缓存过期时间
     * @param clazz 缓存数据类型
     * @param handler   数据源获取类
     * @param <T>
     * @return
     */
    <T> T get(String key, int expiredSeconds, Class<T> clazz, MissCacheHandler<T> handler);


    /**
     * 获取缓存中的数据
     * @param key 缓存key
     * @param type 缓存数据类型(支持泛型)
     * @param <T>
     * @return
     */
    <T> T get(String key, CacheType<T> type);

    /**
     * 获取缓存中的数据, 如果没有key对应的数据, 则从handler中获取并存入缓存中
     * @param key   缓存key
     * @param expiredSeconds 缓存过期时间
     * @param type 缓存数据类型(支持泛型)
     * @param handler   数据源获取类
     * @param <T>
     * @return
     */
    <T> T get(String key, int expiredSeconds, CacheType<T> type, MissCacheHandler<T> handler);
}
