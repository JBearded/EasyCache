# EasyCache

## 本地缓存和远程缓存
下面是本地缓存的基本用法
> LocalCache localCache = new LocalCache();  
> localCache.set("easyCache-local-user", userInfo, 60);  
> UserInfo userInfo = localCache.get("easyCache-local-user", UserInfo.class);  

本地缓存为了避免长期大量占用内存, 可以调用定时零点清除缓存的操作
> localCache.clearScheduler();

下面是远程缓存的基本用法
首先需要实现RemoteCacheInterface的接口, 用于远程缓存的存储和获取
> JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
> jedisPoolConfig.setMaxTotal(100);
> jedisPoolConfig.setMaxIdle(20);
> jedisPoolConfig.setMinIdle(20);
> jedisPoolConfig.setMaxWaitMillis(1000*5);
> RemoteCacheInterface remoteCacheInterface = new RedisCache(jedisPoolConfig, "127.0.0.1", 6380, 1000*5);
> RemoteCache remoteCache = new RemoteCache(remoteCacheInterface);
> 
> remoteCache.set("easyCache-remote-user", userInfo, 60);
> UserInfo userInfo = remoteCache.get("easyCache-remote-user", UserInfo.class);

在构造本地缓存或者远程缓存时, 还可以传入一个配置参数
> CacheConfig config = new CacheConfig(60*60, 32, 2000);
> LocalCache localCache = new LocalCache(config);

这个配置中, 包括了几个参数和默认值

* defaultExpireSeconds = 60 * 60 * 24;  //默认缓存过期的时间
* schedulerCorePoolSize = 64;   //定时器的线程池大小
* retryRegisterDelayMillisSecond = 1000 * 2;    //注册失败后, 延迟多久后再重新注册

## 缓存策略
如果在应用中需要定时刷新数据源到缓存中, 在EasyCache中支持两种缓存策略, 其中就有定时刷新策略. 下面以本地缓存作为例子
> localCache.register("easyCache-local-timing-user", new CachePolicy(delaySeconds, intervalSeconds, new MissCacheHandler<UserInfo>(){    
> &ensp;    @Override  
> &emsp;    public UserInfo getData() {  
> &emsp;&emsp;  return doSomthing();  
> &emsp;    }  
> }));  

以上就注册了定时刷新缓存的策略, 我们只需要在使用到`easyCache-local-timing-user`这个key的地方, 获取数据就可以了. 
> UserInfo userInfo = localCache.get("easyCache-local-timing-user", UserInfo.class);

还有一种策略是过期策略, 就是如果缓存有数据就返回, 没有缓存数据就获取源数据并存入缓存
> localCache.register("easyCache-local-expired-user", new CachePolicy(expiredSeconds, new  MissCacheHandler<UserInfo>(){  
> &ensp;    @Override  
> &emsp;    public UserInfo getData() {  
> &emsp;&emsp;  return doSomthing();  
> &emsp;    }  
> }));  

如果需要传参, 可以在MissCacheHandler构造函数中传入
> localCache.register("easyCache-local-expired-user", new CachePolicy(expiredSeconds, new  MissCacheHandler<UserInfo>(params){  
> &ensp;    @Override  
> &emsp;    public UserInfo getData() {  
> &emsp;&emsp;  return doSomthing(params);  
> &emsp;    }  
> }));    

## 缓存注解
为了更方便使用缓存, EasyCache支持了缓存注解的方式来做方法缓存. 先看一下例子

`UserService.java`  
> @LocalCache(key="$1", expire = 5)  
> public String getUserName(int id) {  
> &ensp;    return doSomething(id);  
> }  

> @LocalCache(key="$1.id$1.pword", expire = 5)  
> public boolean login(UserInfo info){  
> &ensp;    return doSomething(id);  
> }  

其中$1表示第一个参数值, 以此类推, $10表示第10个参数.而$1.name表示第一个参数的name属性值. 最终缓存的key为:  
key = 类名 + 方法名 + 各种参数值($1$3.name$4等等)

> CacheBeanFactory cacheBeanFactory = new CacheBeanFactory();
> cacheBeanFactory.set(localCache.getClass(), localCache);
> cacheBeanFactory.set(remoteCache.getClass(), remoteCache);
> CacheInterceptor cacheInterceptor = new CacheInterceptor(cacheBeanFactory);
> cacheInterceptor.run("com.test");
>
> UserService userService = cacheBeanFactory.get(UserService.class);
> String i = userService.getUserName(3);
> boolean successful = userService.login(new UserInfo(123, "helloworld"));

以上从Bean容器获取的UserService实例在调用getUserName和login方法时, 会先获取缓存数据, 如果没有则会直接调用方法获取源数据, 并保存到缓存中. 
CacheInterceptor的run方法, 会扫描包下的所有带有缓存注解的类, 并生成相应的代理缓存类, 最后覆盖掉在Bean容器中原来的类. 

如果你需要把EasyCache的注解功能用到自己的应用中, 你需要实现BeanFactoryInterface接口, 用于保存和获取在你的应用的Bean实例.如下.

> public class YourCacheBeanFactory implements BeanFactoryInterface{
>
> @Override
> public <T> void set(Class<?> clazz, T object) {
> &ensp;    YourBeanFactory.set(clazz, object);
> }
>
>  @Override
>  public <T> T get(Class<?> clazz) {
>  &ensp;   return YourBeanFactory.get(clazz);
>  }
>  }

&ensp;

> YourCacheBeanFactory cacheBeanFactory = new YourCacheBeanFactory();
> cacheBeanFactory.set(localCache.getClass(), localCache);
> cacheBeanFactory.set(remoteCache.getClass(), remoteCache);
> CacheInterceptor cacheInterceptor = new CacheInterceptor(cacheBeanFactory);
> cacheInterceptor.run("com.test");
>
> UserService userService = cacheBeanFactory.get(UserService.class);
> String i = userService.getUserName(3);
> boolean successful = userService.login(new UserInfo(123, "helloworld"));




