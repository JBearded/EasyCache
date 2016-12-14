# EasyCache 2.0

## 初衷
一开始写EasyCache是不希望在使用缓存的时候, 重复写`if-else`的判空代码, 我希望代码能够替我处理缓存为空的情况.
于是, 在EasyCache中, 你可以这么写:

    MyValue value = easyCache.get(
            key,
            expiredSeconds,
            MyValue.class,
            new MissCacheHandler<MyValue>() {
                @Override
                public MyValue getData() {
                    return getFromDB();
                }
            }
    );

在缓存为空的情况下, EasyCache就会从MissCacheHandler的getData方法中去获取, 并重新做过期缓存.
你也可以传参数给MissCacheHandler, 代码如下:

    MyValue value = easyCache.get(
            key,
            expiredSeconds,
            MyValue.class,
            new MissCacheHandler<MyValue>(p1, p2) {
                @Override
                public MyValue getData() {
                    String p1 = (String)params[0];
                    int p2 = (int)params[1];
                    return getFromDB();
                }
            }
    );

有时候, 因为访问数据库(或第三方接口)的请求太慢, 会定时把数据加载到缓存中, EasyCache也支持定时刷新缓存的功能.

    easyCache.register(interval_key, new CachePolicy(
            delaySeconds,
            intervalSeconds,
            expiredSeconds,
            new MissCacheHandler<YourValue>() {
                @Override
                public YourValue getData() {
                    return getFromDB();
                }
            }
    ));

注册了`interval_key`之后, EasyCache就会延迟`delaySeconds`之后, 间隔`intervalSeconds`定时刷新缓存,
这个缓存会保存`expiredSeconds`(一般expiredSeconds >= intervalSeconds). 之后, 你就可以在任何一个地方
调用`YourValue value = easyCache.get(interval_key, YourValue.class);`

如果觉得`easyCache.get(key, expiredSeconds, MyValue.class, MissCacheHandler handler)`太笨重,
你也可以在初始化的时候注册一个过期缓存策略

    localCache.register(expired_key, new CachePolicy(
            expiredSeconds,
            new MissCacheHandler<MyValue>() {
                @Override
                public MyValue getData() {
                    return getFromDB();
                }
            }
    ));

注册之后, 你也可以在任何一个地方去调用`MyValue value = easyCache.get(interval_key, MyValue.class);`,
如果没有获取到缓存, 就会去注册的MissCacheHandler中去获取值, 并重新做过期缓存.

> 那问题是, 什么对象才能调用这些方法呢. 很简单, 只要继承了AbstractEasyCache, 并实现`setString`和`getString`这两个方法即可


## 注解
在去掉`if-else`的代码后, 还是觉得不够简洁, 于是EasyCache支持了方法的缓存注解`@Cache`和`@LocalCache`

#### @LocalCache
使用这个注解, 就会使用LocalCache这个本地缓存做方法返回值的缓存, 而LocalCache是EasyCache内置的本地缓存类

    @LocalCache(key= "userId_{$1}", expired= 5)
    public UserInfo getUserInfo(int id) {
        return getUserInfoFromDB(id);
    }

以上, 假如id=1234, 那么LocalCache就把方法返回值缓存5秒, key为`类名.getUserInfo_userId_1234`. 正如大家看到的,
`{$1}`即表示这个这个方法的第一个参数值

#### @Cache
使用这个注解, 需要使用到继承了`AbstractEasyCache`的类

    @Cache(instance = RedisCache.class, key = "biz_{$1.biz}_moduleId_{$1.moduleId}", expired = 60)
    public PageData<UserInfo> page(BizModule bm){
        return getUserInfoFromDB(id);
    }

以上, RedisCache就是继承了`AbstractEasyCache`的类. 正如大家看到的, `{$1.biz}`和`{$1.moduleId}`分别表示
这个方法第一个参数的biz属性值和moduleId属性值

#### @DefaultCache
为了让`@Cache`使用起来更加轻便, `@DefaultCache`可以标记在`AbstractEasyCache`的继承类, 表示`@Cache`会把此缓存
类当作默认的缓存

    @Cache(key = "biz_{$1.biz}_moduleId_{$1.moduleId}", expired = 60)
    public PageData<UserInfo> page(BizModule bm){
        return getUserInfoFromDB(id);
    }

## 参数配置
上面已经介绍了两种主要的使用方式. 这里再说下EasyCache的一些参数配置`CacheConfig`

    CacheConfig config = new CacheConfig.Builder()
            .defaultExpiredSeconds(60)
            .clearSchedulerIntervalSeconds(60*60)
            .schedulerCorePoolSize(64)
            .lockSegments(32)
            .lockIsFair(false)
            .avoidServerOverload(true)
            .build();

* defaultExpiredSeconds: 默认的过期时间
* clearSchedulerIntervalSeconds: 定时清除本地缓存的间隔时间
* schedulerCorePoolSize: 定时器的线程池大小(定时刷新缓存和定时清除本地缓存会使用到)
* lockSegments: 分片锁的大小(在处理本地缓存和避免服务过载时会用到)
* lockIsFair: 是否公平锁
* avoidServerOverload: 避免服务过载(当高并发下访问同一个过期的key, 大量的请求就会涌入数据库服务(或第三方服务), 为了避免这种情况, 可以把值设置为true, 默认为false)

## 框架集成

* EasyCache-Spring-Sample: 在spring中的集成示例
