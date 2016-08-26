package com.ecache;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author 谢俊权
 * @create 2016/8/3 14:17
 */
public class CacheType<T> {

    public final Type type;

    protected CacheType() {
        Type superClass = this.getClass().getGenericSuperclass();
        this.type = ((ParameterizedType)superClass).getActualTypeArguments()[0];
    }

    protected CacheType(ParameterizedType type){
        this.type = type;
    }
}
