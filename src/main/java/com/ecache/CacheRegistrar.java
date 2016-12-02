package com.ecache;

/**
 * 公用缓存工具
 * @author 谢俊权
 * @create 2016/7/12 13:49
 */
public interface CacheRegistrar {

    <T> void register(String key, CachePolicy<T> cachePolicy);
}
