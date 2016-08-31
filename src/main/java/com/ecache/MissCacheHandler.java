package com.ecache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 没有命中缓存时, 获取数据源的处理器
 * @author 谢俊权
 * @create 2016/7/9 16:33
 */
public abstract class MissCacheHandler<T> {

    public List<Object> params = new ArrayList<>();

    public MissCacheHandler(){}
    public MissCacheHandler(Object... ps){
        params.addAll(Arrays.asList(ps));
    }

    public abstract T getData();
}
