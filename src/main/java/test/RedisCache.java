package test;

import com.cache.RemoteCacheInterface;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author 谢俊权
 * @create 2016/7/12 13:35
 */
public class RedisCache implements RemoteCacheInterface{

    private JedisPool jedisPool;

    public RedisCache(JedisPoolConfig config, String ip, int port, int timeout) {
        this.jedisPool = new JedisPool(config, ip, port, timeout);
    }

    @Override
    public void set(String key, String value, int expireSeconds) {
        Jedis jedis = null;
        try{
            jedis = jedisPool.getResource();
            jedis.setex(key, expireSeconds, value);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(jedis != null){
                jedis.close();
            }
        }
    }

    @Override
    public String get(String key) {
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
