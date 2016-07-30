package com.ecache.annotation;

import java.util.List;

/**
 * @author 谢俊权
 * @create 2016/7/28 10:40
 */
public class ClassCacheAnnInfo {

    /**
     * 带有缓存注解的类
     */
    private Class<?> clazz;

    /**
     * 方法注解信息列表
     */
    private List<MethodCacheAnnInfo> methodAnnInfoList;


    public ClassCacheAnnInfo() {
    }

    public ClassCacheAnnInfo(Class<?> clazz, List<MethodCacheAnnInfo> methodAnnInfoList) {
        this.clazz = clazz;
        this.methodAnnInfoList = methodAnnInfoList;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }

    public List<MethodCacheAnnInfo> getMethodAnnInfoList() {
        return methodAnnInfoList;
    }

    public void setMethodAnnInfoList(List<MethodCacheAnnInfo> methodAnnInfoList) {
        this.methodAnnInfoList = methodAnnInfoList;
    }
}
