package test;

import com.annotation.LocalCache;

/**
 * @author 谢俊权
 * @create 2016/7/25 9:59
 */
public class UserService {

    @LocalCache(key="$0$1$2$3.name")
    private int getUserName(int id, String h, MyValue myValue) {
        return 1;
    }

}
