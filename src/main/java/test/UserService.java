package test;

import com.annotation.LocalCache;
import com.bean.BeanFactory;

/**
 * @author 谢俊权
 * @create 2016/7/25 9:59
 */
public class UserService {

    @LocalCache(key="$0$1$2$3.name")
    private int getUserName$1(int id, String h, MyValue myValue) {
        return 1;
    }

    public Integer getUserName(int arg1, String arg2, MyValue myValue) {
        String key = "getUserName" + arg1 + myValue.getId();
        com.cache.LocalCache easyCacheObject = BeanFactory.get(com.cache.LocalCache.class);
        Integer returnValue = easyCacheObject.get(key, Integer.class);
        if(returnValue == null) {
            returnValue = this.getUserName$1(arg1, arg2, myValue);
            if(returnValue != null) {
                easyCacheObject.set(key, returnValue, 1000000);
            }
        }

        return returnValue;
    }
}
