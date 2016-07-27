package test;

import com.annotation.LocalCache;

/**
 * @author 谢俊权
 * @create 2016/7/25 9:59
 */
public class UserService {

    @LocalCache
    private String getUserName(int id, String name) {
        return "welcome "+id+name;
    }
}
