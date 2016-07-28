package com.bean;

/**
 * @author 谢俊权
 * @create 2016/7/17 17:58
 */
public class CacheBeanFactory implements BeanFactoryInterface{

    @Override
    public <T> void set(Class<?> clazz, T object) {
        CacheObjectMapping.set(clazz, object);
    }

    @Override
    public <T> T get(Class<?> clazz) {
        return CacheObjectMapping.get(clazz);
    }
}
