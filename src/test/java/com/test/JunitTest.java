package com.test;

import com.ecache.*;
import com.ecache.bean.CacheBeanFactory;
import com.ecache.proxy.CacheInterceptor;
import org.junit.Ignore;
import org.junit.Test;
import redis.clients.jedis.JedisPoolConfig;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author xiejunquan
 * @create 2016/11/23 10:34
 */
public class JunitTest {

    @Ignore
    @Test
    public void getTest(){
        EasyCache easyCache = getRedisCache();
        String key = "easy_cache";
        int expiredSeconds = 60;
        MyValue value = easyCache.get(
                key,
                expiredSeconds,
                MyValue.class,
                new MissCacheHandler<MyValue>() {
                    @Override
                    public MyValue getData() {
                        return null;
                    }
                }
        );
    }

    @Ignore
    @Test
    public void registerTest() throws Exception{

        EasyCache localCache = getRedisCache();

        AtomicInteger localExpireNumber = new AtomicInteger(0);
        AtomicInteger localIntervalNumber = new AtomicInteger(0);

        /*注册过期缓存策略*/
        int expiredSeconds = 10;
        String expired_key = "local-expire-key";
        localCache.register(expired_key, new CachePolicy(
                expiredSeconds,
                new MissCacheHandler<MyValue>(localExpireNumber) {
                    @Override
                    public MyValue getData() {
                        AtomicInteger localExpireNumber = (AtomicInteger) this.params[0];
                        int number = localExpireNumber.incrementAndGet();
                        System.out.printf("%d: ---expire get data---\n", number);
                        return new MyValue(number, "local-expire-value");
                    }
                }
        ));
        /*注册定时刷新缓存策略*/
        int delaySeconds = 0;
        int intervalSeconds = 2;
        String interval_key = "local-interval-key";
        localCache.register(interval_key, new CachePolicy(
                delaySeconds,
                intervalSeconds,
                expiredSeconds,
                new MissCacheHandler<YourValue>(localIntervalNumber) {
                    @Override
                    public YourValue getData() {
                        AtomicInteger localIntervalNumber = (AtomicInteger) this.params[0];
                        int number = localIntervalNumber.incrementAndGet();
                        System.out.printf("%d: ---interval get data---\n", number);
                        return new YourValue(number, "local-interval-value");
                    }
                }
        ));
        Thread.sleep(1000 * 5);
        MyValue localExpireMyValue = localCache.get(expired_key, MyValue.class);
        System.out.println(localExpireMyValue.getId());
        Thread.sleep(1000 * 5);
        localExpireMyValue = localCache.get(expired_key, MyValue.class);
        System.out.println(localExpireMyValue.getId());

        Thread.sleep(1000 * 5);
        YourValue localIntervalMyValue = localCache.get(interval_key, YourValue.class);
        System.out.println(localIntervalMyValue.getId());
        Thread.sleep(1000 * 5);
        localIntervalMyValue = localCache.get(interval_key, YourValue.class);
        System.out.println(localIntervalMyValue.getId());
    }


    @Test
    public void annotationTest() throws Exception{

        LocalCache localCache = getLocalCache();
        EasyCache easyCache = getRedisCache();

        CacheBeanFactory cacheBeanFactory = new CacheBeanFactory();
        cacheBeanFactory.set(LocalCache.class, localCache);
        cacheBeanFactory.set(RedisCache.class, easyCache);
        CacheInterceptor cacheInterceptor = new CacheInterceptor(cacheBeanFactory);
        cacheInterceptor.run();

        UserService userService = cacheBeanFactory.get(UserService.class);
        String name = userService.getUserName(1);
        System.out.println(name);
        Thread.sleep(1000 * 2);
        name = userService.getUserName(1);  //缓存
        System.out.println(name);
        Thread.sleep(1000 * 2);
        name = userService.getUserName(3);
        System.out.println(name);
        Thread.sleep(1000 * 2);
        name = userService.getUserName(3);  //缓存
        System.out.println(name);

        List<UserInfo> userInfoList = userService.getUserInfo(2);
        System.out.println(userInfoList.get(0).getName());
        Thread.sleep(1000 * 2);
        userInfoList = userService.getUserInfo(2);  //缓存
        System.out.println(userInfoList.get(0).getName());
    }

    @Ignore
    @Test
    public void threadTest() throws Exception {

        final EasyCache redisCache = getRedisCache();

        AtomicInteger dbNumber = new AtomicInteger(0);
        String key = "cache-thread-key";
        int expiredSeconds = 1;
        redisCache.register(key, new CachePolicy(
                expiredSeconds,
                new MissCacheHandler<Integer>(dbNumber) {
                    @Override
                    public Integer getData() {
                        AtomicInteger dbNumber = (AtomicInteger) this.params[0];
                        int number = dbNumber.incrementAndGet();
                        System.out.println(number +" : "+ Thread.currentThread().getName()+": get data from db");
                        return number;
                    }
                }
        ));
        ExecutorService executorService = Executors.newFixedThreadPool(100);
        final CountDownLatch begin = new CountDownLatch(1);
        for (int i = 1; i <= 1000; i++) {
            if (i % 100 == 0) {
                Thread.sleep(1000);
                begin.countDown();
            }
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        begin.await();
                        Integer number = redisCache.get("cache-thread-key", Integer.class);
                        System.out.println(number);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        executorService.shutdown();
        Thread.sleep(1000*30);
    }


    public LocalCache getLocalCache(){
        CacheConfig config = new CacheConfig.Builder()
                .defaultExpiredSeconds(60)
                .schedulerCorePoolSize(64)
                .lockSegments(32)
                .lockIsFair(false)
                .avoidServerOverload(true)
                .clearSchedulerIntervalSeconds(60*60)
                .build();

        LocalCache localCache = new LocalCache(config);
        return localCache;
    }

    public EasyCache getRedisCache(){
        CacheConfig config = new CacheConfig.Builder()
                .defaultExpiredSeconds(60)
                .schedulerCorePoolSize(64)
                .lockSegments(32)
                .lockIsFair(false)
                .avoidServerOverload(true)
                .clearSchedulerIntervalSeconds(60*60)
                .build();

        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(100);
        jedisPoolConfig.setMaxIdle(20);
        jedisPoolConfig.setMinIdle(20);
        jedisPoolConfig.setMaxWaitMillis(1000*5);

        EasyCache redisCache = new RedisCache(config, jedisPoolConfig, "127.0.0.1", 6380, 1000*5);
        return redisCache;
    }


    static class MyValue{
        private int id;
        private String name;

        public MyValue() {
        }

        public MyValue(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    static class YourValue{
        private int id;
        private String name;

        public YourValue() {
        }

        public YourValue(int id, String name) {
            this.id = id;
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
