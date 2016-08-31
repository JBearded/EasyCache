package test;

import com.ecache.*;
import com.ecache.bean.CacheBeanFactory;
import com.ecache.proxy.CacheInterceptor;
import redis.clients.jedis.JedisPoolConfig;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 谢俊权
 * @create 2016/7/9 18:54
 */
public class Main {

    public static void main(String[] args) throws Exception {

        CacheConfig config = new CacheConfig.Builder()
                .defaultExpiredSeconds(60)
                .schedulerCorePoolSize(64)
                .lockSegments(32)
                .lockIsFair(false)
                .avoidServerOverload(true)
                .clearSchedulerIntervalSeconds(60*60)
                .build();

        LocalCache localCache = new LocalCache(config);

        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(100);
        jedisPoolConfig.setMaxIdle(20);
        jedisPoolConfig.setMinIdle(20);
        jedisPoolConfig.setMaxWaitMillis(1000*5);
        RedisCache redisCache = new RedisCache(jedisPoolConfig, "127.0.0.1", 6380, 1000*5);
        RemoteCache remoteCache = new RemoteCache(config, redisCache);

//        registerTest(remoteCache, localCache);
//        annotationTest(remoteCache, redisCache, localCache);
//        threadTest(remoteCache, localCache);
        annoThreadTest(remoteCache, localCache, redisCache);
    }

    public static void registerTest(RemoteCache remoteCache, LocalCache localCache) throws Exception{

        final AtomicInteger localExpireNumber = new AtomicInteger(0);
        final AtomicInteger localIntervalNumber = new AtomicInteger(0);
        /*注册过期缓存策略*/
        localCache.register("local-expire-key", new CachePolicy(10, new MissCacheHandler<MyValue>() {
            @Override
            public MyValue getData() {
                return new MyValue(localExpireNumber.incrementAndGet(), "local-expire-value");
            }
        }));
        /*注册定时刷新缓存策略*/
        localCache.register("local-interval-key", new CachePolicy(0, 2, new MissCacheHandler<YourValue>() {
            @Override
            public YourValue getData() {
                return new YourValue(localIntervalNumber.incrementAndGet(), "local-interval-value");
            }
        }));
        Thread.sleep(1000 * 5);
        MyValue localExpireMyValue = localCache.get("local-expire-key", MyValue.class);
        System.out.println(localExpireMyValue.getId());
        Thread.sleep(1000 * 5);
        localExpireMyValue = localCache.get("local-expire-key", MyValue.class);
        System.out.println(localExpireMyValue.getId());
        Thread.sleep(1000 * 5);
        YourValue localIntervalMyValue = localCache.get("local-interval-key", YourValue.class);
        System.out.println(localIntervalMyValue.getId());
        Thread.sleep(1000 * 5);
        localIntervalMyValue = localCache.get("local-interval-key", YourValue.class);
        System.out.println(localIntervalMyValue.getId());
        /*即时获取缓存*/
        int myId = 1024;
        System.out.println(localCache.get("local-site-key", 100, MyValue.class, new MissCacheHandler<MyValue>(myId) {
            @Override
            public MyValue getData() {
                return new MyValue((Integer) params.get(0), "local-site-value");
            }
        }).getId());



        final AtomicInteger remoteExpireNumber = new AtomicInteger(0);
        final AtomicInteger remoteIntervalNumber = new AtomicInteger(0);
        /*注册过期缓存策略*/
        remoteCache.register("remote-expire-key", new CachePolicy<MyValue>(10, new MissCacheHandler<MyValue>() {
            @Override
            public MyValue getData() {
                return new MyValue(remoteExpireNumber.incrementAndGet(), "remote-expire-value");
            }
        }));
        /*注册定时刷新缓存策略*/
        remoteCache.register("remote-interval-key", new CachePolicy<YourValue>(0, 2, new MissCacheHandler<YourValue>() {
            @Override
            public YourValue getData() {
                return new YourValue(remoteIntervalNumber.incrementAndGet(), "remote-interval-value");
            }
        }));


        Thread.sleep(1000 * 5);
        MyValue remoteExpireMyValue = remoteCache.get("remote-expire-key", MyValue.class);
        System.out.println(remoteExpireMyValue.getId());
        Thread.sleep(1000 * 5);
        remoteExpireMyValue = remoteCache.get("remote-expire-key", MyValue.class);
        System.out.println(remoteExpireMyValue.getId());

        Thread.sleep(1000 * 5);
        YourValue remoteIntervalMyValue = remoteCache.get("remote-interval-key", YourValue.class);
        System.out.println(remoteIntervalMyValue.getId());
        Thread.sleep(1000 * 5);
        remoteIntervalMyValue = remoteCache.get("remote-interval-key", YourValue.class);
        System.out.println(remoteIntervalMyValue.getId());

        /*即时获取缓存*/
        System.out.println(remoteCache.get("remote-site-key", 100, MyValue.class, new MissCacheHandler<MyValue>(myId) {
            @Override
            public MyValue getData() {
                return new MyValue((Integer) params.get(0), "remote-site-value");
            }
        }).getId());
    }

    public static void annotationTest(RemoteCache remoteCache, RedisCache redisCache, LocalCache localCache) throws Exception{
        CacheBeanFactory cacheBeanFactory = new CacheBeanFactory();
        cacheBeanFactory.set(localCache.getClass(), localCache);
        cacheBeanFactory.set(remoteCache.getClass(), remoteCache);
        cacheBeanFactory.set(redisCache.getClass(), "localRedisCache", redisCache);
        CacheInterceptor cacheInterceptor = new CacheInterceptor(cacheBeanFactory);
        cacheInterceptor.run("test");

        UserService userService = cacheBeanFactory.get(UserService.class);
        String i = userService.getUserName(1);
        System.out.println(i);
        Thread.sleep(1000 * 2);
        i = userService.getUserName(1);
        System.out.println(i);
        Thread.sleep(1000 * 2);
        i = userService.getUserName(3);
        System.out.println(i);

        boolean successful = userService.login(new UserInfo(1, "123"));
        System.out.println(successful);
        Thread.sleep(1000 * 1);
        successful = userService.login(new UserInfo(1, "123"));
        System.out.println(successful);
        Thread.sleep(1000 * 1);
        successful = userService.login(new UserInfo(2, "234"));
        System.out.println(successful);
        Thread.sleep(1000 * 1);
    }

    public static void threadTest(final RemoteCache remoteCache, final LocalCache localCache) throws Exception {

        final AtomicInteger dbNumber = new AtomicInteger(0);
        remoteCache.register("ecache-thread-key", new CachePolicy(2, new MissCacheHandler<String>() {
            @Override
            public String getData() {
                System.out.println(dbNumber.incrementAndGet() +" : "+ Thread.currentThread().getName()+": get data from db");
                return "hello";
            }
        }));
        ExecutorService executorService = Executors.newFixedThreadPool(100);
        final CountDownLatch begin = new CountDownLatch(1);
        for (int i = 1; i <= 1000; i++) {
            if (i % 100 == 0) {
                begin.countDown();
                System.out.println("--------------------------------------------------");
            }
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        begin.await();
                        remoteCache.get("ecache-thread-key", String.class);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        executorService.shutdown();
    }

    public static void annoThreadTest(RemoteCache remoteCache, LocalCache localCache, RedisCache redisCache) throws Exception {


        final CacheBeanFactory cacheBeanFactory = new CacheBeanFactory();
        cacheBeanFactory.set(localCache.getClass(), localCache);
        cacheBeanFactory.set(remoteCache.getClass(), remoteCache);
        cacheBeanFactory.set(redisCache.getClass(), "localRedisCache", redisCache);
        CacheInterceptor cacheInterceptor = new CacheInterceptor(cacheBeanFactory);
        cacheInterceptor.run("test");

        ExecutorService executorService = Executors.newFixedThreadPool(100);
        final CountDownLatch begin = new CountDownLatch(1);
        for (int i = 1; i <= 1000; i++) {
            if(i == 1000){
                begin.countDown();
            }
            final int id = i % 3 + 1;
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        begin.await();
                        UserService userService = cacheBeanFactory.get(UserService.class);
                        List<UserInfo> userInfos = userService.getUserInfo(id);
                        UserInfo userInfo = userInfos.get(0);
                        System.out.println(userInfo.getName());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        executorService.shutdown();
    }

    static class MyValue{
        private int id;
        private String name;

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
