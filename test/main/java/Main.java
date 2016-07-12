import com.cache.CacheConfig;
import com.cache.CachePloy;
import com.cache.LocalCache;
import com.cache.MissCacheHandler;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 谢俊权
 * @create 2016/7/9 18:54
 */
public class Main {

    public static void main(String[] args) throws InterruptedException {

        final AtomicInteger number = new AtomicInteger(1);
        CacheConfig config = new CacheConfig(60*60, 32, 2000);
        LocalCache localCache = new LocalCache(config);
        localCache.register("key", new CachePloy(5, new MissCacheHandler<MyValue>() {
            @Override
            public MyValue getData() {
                return new MyValue(number.incrementAndGet(), "myvalue");
            }
        }));
        System.out.println(localCache.<MyValue>get("key").getId());
        Thread.sleep(1000);
        System.out.println(localCache.get("key", MyValue.class).getId());
        Thread.sleep(1000*5);
        System.out.println(localCache.get("key", MyValue.class).getId());
        Thread.sleep(1000*5);
        System.out.println(localCache.get("key", MyValue.class).getId());
        Thread.sleep(1000*3);
        System.out.println(localCache.get("key", MyValue.class).getId());
    }

    static class MyValue {
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
}
