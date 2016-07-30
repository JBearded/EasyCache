package com.ecache;

/**
 * 远程缓存接口, 提供给各种缓存实现
 * @author 谢俊权
 * @create 2016/7/12 13:32
 */
public interface RemoteCacheInterface {

    void set(String key, String value, int expiredSeconds);

    String get(String key);
}
