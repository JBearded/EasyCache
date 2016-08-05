package com.ecache.bean;

/**
 * @author 谢俊权
 * @create 2016/7/27 21:07
 */
public interface BeanFactoryInterface {

    /**
     * 将实例存入Bean容器中
     * @param clazz 实例类型
     * @param object 实例
     * @param <T>
     */
    <T> void set(Class<?> clazz, T object);

    /**
     * 将实例存入Bean容器中
     * @param clazz 实例类型
     * @param id    唯一id
     * @param object    实例
     * @param <T>
     */
    <T> void set(Class<?> clazz, String id, T object);

    /**
     * 获取Bean容器中的实例
     * @param clazz 实例类型
     * @param <T>
     * @return
     */
    <T> T get(Class<?> clazz);

    /**
     * 获取Bean容器中的实例
     * @param clazz 实例类型
     * @param id    唯一id
     * @param <T>
     * @return
     */
    <T> T get(Class<?> clazz, String id);
}
