package com.ecache;

/**
 * @author xiejunquan
 * @create 2016/12/2 9:42
 */
public interface EasyCacheSource {

    /**
     * 保存String数据到缓存中
     * @param key 缓存key
     * @param value 缓存数据
     * @param expiredSeconds 缓存过期时间
     * @return
     */
    String setString(String key, String value, int expiredSeconds);

    /**
     * 获取缓存中的String数据
     * @param key 缓存key
     * @return
     */
    String getString(String key);
}
