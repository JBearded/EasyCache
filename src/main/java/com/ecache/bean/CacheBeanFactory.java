package com.ecache.bean;

/**
 * @author 谢俊权
 * @create 2016/7/17 17:58
 */
public class CacheBeanFactory implements BeanFactoryInterface{

    @Override
    public <T> void set(Class<?> clazz, T object) {
        CacheObjectMapping.set(clazz.getName(), object);
    }

    @Override
    public <T> void set(Class<?> clazz, String id, T object) {
        String key = getBeanKey(clazz, id);
        CacheObjectMapping.set(key, object);
        CacheObjectMapping.set(clazz.getName(), object);
        if(id != null && !"".equals(id)){
            CacheObjectMapping.set(id, object);
        }
    }

    @Override
    public <T> T get(Class<?> clazz) {
        return CacheObjectMapping.get(clazz.getName());
    }

    @Override
    public <T> T get(Class<?> clazz, String id) {
        String key = getBeanKey(clazz, id);
        return CacheObjectMapping.get(key);
    }

    private String getBeanKey(Class<?> clazz, String id){
        String key = clazz.getName();
        if(id != null && !"".equals(id)){
            key = key + "|" + id;
        }
        return key;
    }
}
