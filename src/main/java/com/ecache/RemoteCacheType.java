package com.ecache;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author 谢俊权
 * @create 2016/8/3 14:17
 */
public class RemoteCacheType<T> {

    protected final Type type;

    protected RemoteCacheType() {
        Type superClass = this.getClass().getGenericSuperclass();
        this.type = ((ParameterizedType)superClass).getActualTypeArguments()[0];
    }

    protected RemoteCacheType(ParameterizedType type){
        this.type = type;
    }
}
