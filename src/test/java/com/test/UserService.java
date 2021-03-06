package com.test;

import com.ecache.annotation.Cache;
import com.ecache.annotation.LocalCache;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 谢俊权
 * @create 2016/7/25 9:59
 */
public class UserService {

    private static Map<Integer, UserInfo> userMap = new HashMap<>();
    static{
        userMap.put(1, new UserInfo(1, "xie", "123"));
        userMap.put(2, new UserInfo(2, "jun", "234"));
        userMap.put(3, new UserInfo(3, "quan", "345"));
    }

    @Cache(key = "userId_$1", expired = 5)
    public String getUserName(int id) {
        System.out.println("get user name from db");
        UserInfo user = userMap.get(id);
        if(user != null){
            return user.getName();
        }
        return null;
    }

    @LocalCache(key= "userId_{$1}", expired= 5)
    public List<UserInfo> getUserInfo(int id) {
        System.out.println("get user info from db");
        return Arrays.asList(userMap.get(id));
    }

}
