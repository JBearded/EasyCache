package com.bean;

/**
 * @author 谢俊权
 * @create 2016/7/27 21:07
 */
public interface BeanFactoryInterface {

    <T> void set(Class<?> clazz, T object);

    <T> T get(Class<?> clazz);
}
