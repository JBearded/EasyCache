package com.ecache;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author 谢俊权
 * @create 2016/8/3 14:17
 */
public class CacheType<T> {

    public final Type actualType;

    protected CacheType() {
        Type superClass = this.getClass().getGenericSuperclass();
        this.actualType = ((ParameterizedType)superClass).getActualTypeArguments()[0];
    }

    protected CacheType(ParameterizedType type){
        this.actualType = type;
    }

    protected CacheType(Class type){
        this.actualType = type;
    }
}
