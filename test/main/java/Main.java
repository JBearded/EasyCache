import com.cache.*;
import redis.clients.jedis.JedisPoolConfig;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 谢俊权
 * @create 2016/7/9 18:54
 */
public class Main {

    public static void main(String[] args) throws InterruptedException {

        CacheConfig config = new CacheConfig(60*60, 32, 2000);

        final AtomicInteger localExpireNumber = new AtomicInteger(0);
        final AtomicInteger localIntervalNumber = new AtomicInteger(0);
        LocalCache localCache = new LocalCache(config);
        localCache.register("local-expire-key", new CachePloy(10, new MissCacheHandler<MyValue>() {
            @Override
            public MyValue getData() {
                return new MyValue(localExpireNumber.incrementAndGet(), "local-expire-value");
            }
        }));
        localCache.register("local-interval-key", new CachePloy(0, 2, new MissCacheHandler<MyValue>() {
            @Override
            public MyValue getData() {
                return new MyValue(localIntervalNumber.incrementAndGet(), "local-interval-value");
            }
        }));

        Thread.sleep(1000 * 5);
        MyValue localExpireMyValue = localCache.get("local-expire-key", MyValue.class);
        System.out.println(localExpireMyValue.getId());
        Thread.sleep(1000 * 5);
        localExpireMyValue = localCache.get("local-expire-key", MyValue.class);
        System.out.println(localExpireMyValue.getId());

        Thread.sleep(1000 * 5);
        MyValue localIntervalMyValue = localCache.get("local-interval-key", MyValue.class);
        System.out.println(localIntervalMyValue.getId());
        Thread.sleep(1000 * 5);
        localIntervalMyValue = localCache.get("local-interval-key", MyValue.class);
        System.out.println(localIntervalMyValue.getId());


        final AtomicInteger remoteExpireNumber = new AtomicInteger(0);
        final AtomicInteger remoteIntervalNumber = new AtomicInteger(0);
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(100);
        jedisPoolConfig.setMaxIdle(20);
        jedisPoolConfig.setMinIdle(20);
        jedisPoolConfig.setMaxWaitMillis(1000*5);
        RemoteCacheInterface remoteCacheInterface = new RedisCache(jedisPoolConfig, "127.0.0.1", 6379, 1000*5);
        RemoteCache remoteCache = new RemoteCache(config, remoteCacheInterface);
        remoteCache.register("remote-expire-key", new CachePloy(10, new MissCacheHandler<MyValue>() {
            @Override
            public MyValue getData() {
                return new MyValue(remoteExpireNumber.incrementAndGet(), "remote-expire-value");
            }
        }));
        remoteCache.register("remote-interval-key", new CachePloy(0, 2, new MissCacheHandler<MyValue>() {
            @Override
            public MyValue getData() {
                return new MyValue(remoteIntervalNumber.incrementAndGet(), "remote-interval-value");
            }
        }));


        Thread.sleep(1000 * 5);
        MyValue remoteExpireMyValue = remoteCache.get("remote-expire-key", MyValue.class);
        System.out.println(remoteExpireMyValue.getId());
        Thread.sleep(1000 * 5);
        remoteExpireMyValue = remoteCache.get("remote-expire-key", MyValue.class);
        System.out.println(remoteExpireMyValue.getId());

        Thread.sleep(1000 * 5);
        MyValue remoteIntervalMyValue = remoteCache.get("remote-interval-key", MyValue.class);
        System.out.println(remoteIntervalMyValue.getId());
        Thread.sleep(1000 * 5);
        remoteIntervalMyValue = remoteCache.get("remote-interval-key", MyValue.class);
        System.out.println(remoteIntervalMyValue.getId());

    }

    static class MyValue {
        private int id;
        private String name;

        public MyValue(){}
        public MyValue(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public void setId(int id) {
            this.id = id;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }
}
