package com.ecache;

/**
 * 没有命中缓存时, 获取数据源的处理器
 * @author 谢俊权
 * @create 2016/7/9 16:33
 */
public abstract class MissCacheHandler<T> {

    public Object params;

    public MissCacheHandler(){}
    public MissCacheHandler(Object params){
        this.params = params;
    }

    public abstract T getData();
}
