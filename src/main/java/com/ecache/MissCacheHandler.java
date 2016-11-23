package com.ecache;

/**
 * 没有命中缓存时, 获取数据源的处理器
 * @author 谢俊权
 * @create 2016/7/9 16:33
 */
public abstract class MissCacheHandler<T> {

    public Object[] params;

    public MissCacheHandler(){
        this.params = new Object[]{};
    }
    public MissCacheHandler(Object... ps){
        int length = ps.length, index = 0;
        this.params = new Object[length];
        for (Object object : ps) {
            this.params[index++] = object;
        }
    }

    public abstract T getData();
}
