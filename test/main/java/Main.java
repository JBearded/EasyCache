import com.cache.*;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 谢俊权
 * @create 2016/7/9 18:54
 */
public class Main {

    public static void main(String[] args) throws InterruptedException {

        final AtomicInteger number = new AtomicInteger(1);
        LocalCache localCache = new LocalCache();
        localCache.register("key", new CachePloy<String>(5, new MissCacheHandler<String>() {
            @Override
            public String getData() {
                return String.valueOf(number.incrementAndGet());
            }
        }));
        System.out.println(localCache.get("key"));
        Thread.sleep(1000);
        System.out.println(localCache.get("key"));
        Thread.sleep(1000*5);
        System.out.println(localCache.get("key"));
        Thread.sleep(1000*5);
        System.out.println(localCache.get("key"));
        Thread.sleep(1000*3);
        System.out.println(localCache.get("key"));
    }
}
