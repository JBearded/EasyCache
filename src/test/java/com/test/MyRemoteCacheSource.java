package com.test;

import com.ecache.RemoteCacheSource;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author xiejunquan
 * @create 2016/12/2 13:54
 */
public class MyRemoteCacheSource implements RemoteCacheSource {

    private JedisPool jedisPool;

    public MyRemoteCacheSource(JedisPoolConfig jedisPoolConfig, String ip, int port, int timeout) {
        this.jedisPool = new JedisPool(jedisPoolConfig, ip, port, timeout);
    }

    @Override
    public String setString(String key, String value, int expiredSeconds) {
        Jedis jedis = null;
        try{
            jedis = jedisPool.getResource();
            jedis.setex(key, expiredSeconds, value);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(jedis != null){
                jedis.close();
            }
        }
        return value;
    }

    @Override
    public String getString(String key) {
        Jedis jedis = null;
        try{
            jedis = jedisPool.getResource();
            return jedis.get(key);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(jedis != null){
                jedis.close();
            }
        }
        return null;
    }
}
