package com.bean;

/**
 * @author 谢俊权
 * @create 2016/7/28 20:16
 */
public interface InjectorInterface {

    <T> void doInject(T bean);
}
