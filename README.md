# EasyCache

## 本地缓存和远程缓存
下面是本地缓存的基本用法

    LocalCache localCache = new LocalCache();
    localCache.set("easyCache-local-user", userInfo, 60);
    UserInfo userInfo = localCache.get("easyCache-local-user", UserInfo.class);

本地缓存为了避免长期大量占用内存, 会定时清除缓存, 而间隔时间可以在配置中设置clearSchedulerIntervalSeconds.

下面是远程缓存的基本用法
首先需要实现RemoteCacheInterface的接口, 用于远程缓存的存储和获取

    JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
    jedisPoolConfig.setMaxTotal(100);
    jedisPoolConfig.setMaxIdle(20);
    jedisPoolConfig.setMinIdle(20);
    jedisPoolConfig.setMaxWaitMillis(1000*5);
    RemoteCacheInterface remoteCacheInterface = new RedisCache(jedisPoolConfig, "127.0.0.1", 6380, 1000*5);

    RemoteCache remoteCache = new RemoteCache(remoteCacheInterface);
    remoteCache.set("easyCache-remote-user", userInfo, 60);
    UserInfo userInfo = remoteCache.get("easyCache-remote-user", UserInfo.class);


在构造本地缓存或者远程缓存时, 还可以传入一个配置参数

    CacheConfig config = new CacheConfig.Builder()
        .defaultExpiredSeconds(60)
        .schedulerCorePoolSize(64)
        .retryRegisterMSeconds(500)
        .lockSegments(32)
        .lockIsFair(false)
        .avoidServerOverload(true)
        .clearSchedulerIntervalSeconds(60*60)
        .build();

这个配置中, 包括了几个参数和默认值

* defaultExpiredSeconds = 60 * 60;  //默认缓存过期的时间
* schedulerCorePoolSize = 64;   //定时器的线程池大小
* retryRegisterMSeconds = 1000 * 2;    //注册失败后, 延迟多久后再重新注册
* lockSegments = 32;    //分段锁的段数
* lockIsFair = false;   //是否公平锁
* avoidServerOverload = false;  //是否避免数据服务器过载, 用于远程缓存
* clearSchedulerIntervalSeconds = 60 * 60 * 24; //默认定时清除过期缓存的间隔时间, 用于本地缓存


## 缓存策略
如果在应用中需要定时刷新数据源到缓存中, 在EasyCache中支持两种缓存策略, 其中就有定时刷新策略. 下面以本地缓存作为例子

    localCache.register("easyCache-local-timing-user", new CachePolicy(delaySeconds, intervalSeconds, new MissCacheHandler<UserInfo>(){
        @Override
        public UserInfo getData() {
            return doSomething();
        }
    }));

以上就注册了定时刷新缓存的策略, 我们只需要在使用到`easyCache-local-timing-user`这个key的地方, 获取数据就可以了.

    UserInfo userInfo = localCache.get("easyCache-local-timing-user", UserInfo.class);

还有一种策略是过期策略, 就是如果缓存有数据就返回, 没有缓存数据就获取源数据并存入缓存

    localCache.register("easyCache-local-expired-user", new CachePolicy(expiredSeconds, new  MissCacheHandler<UserInfo>(){
        @Override
        public UserInfo getData() {
            return doSomthing();
        }
    }));

如果需要传参, 可以在MissCacheHandler构造函数中传入

    localCache.register("easyCache-local-expired-user", new CachePolicy(expiredSeconds, new  MissCacheHandler<UserInfo>(params){
        @Override
        public UserInfo getData() {
            return doSomthing(params);
        }
    }));

## 缓存注解
为了更方便使用缓存, EasyCache支持了缓存注解的方式来做方法缓存. 先看一下例子`UserService.java`

    @Cache(instance = RedisCache.class, key="$1", expire = 5)
    public String getUserName(int id) {
        return doSomething(id);
    }

    @LocalCache(key="$1.id$1.pword", expire = 5)
    public boolean login(UserInfo info){
        return doSomething(id);
    }

    @RemoteCache(key="$1", expire = 5, avoidOverload = true)
    public boolean getUser(int id){
        return doSomething(id);
    }

@Cache注解用于自定义的缓存实例，需要实现CacheInterface的接口。而@RemoteCache的缓存实现也要实现CacheInterface的接口。
@LocalCache则是本地缓存，已经有了内置实现。在key注解参数中，$1表示第一个参数值, 以此类推, $10表示第10个参数。
而$1.name表示第一个参数的name属性值. 最终缓存的key为: key = 类名 + 方法名 + 各种参数值($1$3.name$4等等)

在使用远程缓存或者自定义缓存注解之前，需要先实现CacheInterface的接口

    public class RedisCache implements CacheInterface {

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

然后我们就可以做初始化工作了

    CacheConfig config = new CacheConfig.Builder()
            .defaultExpiredSeconds(60)
            .schedulerCorePoolSize(64)
            .retryRegisterMSeconds(500)
            .lockSegments(32)
            .lockIsFair(false)
            .avoidServerOverload(false)
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

    CacheBeanFactory cacheBeanFactory = new CacheBeanFactory();
    cacheBeanFactory.set(localCache.getClass(), localCache);
    cacheBeanFactory.set(remoteCache.getClass(), remoteCache);
    cacheBeanFactory.set(redisCache.getClass(), redisCache);

    CacheInterceptor cacheInterceptor = new CacheInterceptor(cacheBeanFactory);
    cacheInterceptor.run("com.test");

    UserService userService = cacheBeanFactory.get(UserService.class);
    String i = userService.getUserName(3);
    boolean successful = userService.login(new UserInfo(123, "helloworld"));

以上从Bean容器获取的UserService实例在调用getUserName和login方法时, 会先获取缓存数据, 如果没有则会直接调用方法获取源数据, 并保存到缓存中. 
CacheInterceptor的run方法, 会扫描包下的所有带有缓存注解的类, 并生成相应的代理缓存类, 最后覆盖掉在Bean容器中原来的类. 

如果你需要把EasyCache的注解功能用到自己的应用中, 你需要实现BeanFactoryInterface接口, 用于保存和获取在你的应用的Bean实例.如下.

    public class YourCacheBeanFactory implements BeanFactoryInterface{
        @Override
        public <T> void set(Class<?> clazz, T object) {
            YourBeanFactory.set(clazz, object);
        }

        @Override
        public <T> void set(Class<?> clazz, String id, T object) {
            YourBeanFactory.set(clazz, id, object);
        }

        @Override
        public <T> T get(Class<?> clazz) {
            return YourBeanFactory.get(clazz);
        }

        @Override
        public <T> T get(Class<?> clazz, String id) {
            return YourBeanFactory.get(clazz, id);
        }
    }

然后就可以写自己的业务代码了

    YourCacheBeanFactory cacheBeanFactory = new YourCacheBeanFactory();
    cacheBeanFactory.set(localCache.getClass(), localCache);
    cacheBeanFactory.set(remoteCache.getClass(), remoteCache);

    CacheInterceptor cacheInterceptor = new CacheInterceptor(cacheBeanFactory);
    cacheInterceptor.run("com.test");
    UserService userService = cacheBeanFactory.get(UserService.class);
    String i = userService.getUserName(3);
    boolean successful = userService.login(new UserInfo(123, "helloworld"));

## 避免数据服务器过载
在使用了缓存的应用中, 可能会出现相同key在高并发中都没有命中缓存的情况, 那么这时候每个请求都会从数据源服务端中获取新的数据下来.
为了避免每个线程都重新从数据源服务端中获取一次数据, EasyCache做了这样的缓存处理, 如果发现没有命中缓存, 就会启用双重检查的加锁机制.
这保证只有第一个拿到锁的线程会从数据源服务端中获取数据并放入缓存中,后面进来的线程还是会重新从缓存中获取数据.虽然这样做减轻了数据源服务端的访问压力,
但是会对缓存服务做两次get操作. 所以这需要使用者来决定是减轻数据源服务端还是缓存服务的压力.这个参数可以在CacheConfig
中设置, 参数名为avoidServerOverload,也在可以在方法注解中设置这个参数,默认都是false, 即关闭了这个机制.
有人可能担心锁是否影响了EasyCache的缓存访问性能, 是的, 加锁确实会影响性能. 但是这个锁是细颗粒度的, 它不会影响不同key的访问, 只会阻塞相同key在线程并发下的get访问.





