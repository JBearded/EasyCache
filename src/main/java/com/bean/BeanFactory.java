package com.bean;

/**
 * @author 谢俊权
 * @create 2016/7/17 17:58
 */
public interface BeanFactory {

    <T> T set(Class<T> tClass);
}
