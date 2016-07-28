package com.annotation;

import java.util.List;

/**
 * @author 谢俊权
 * @create 2016/7/28 10:40
 */
public class ClassCacheAnnInfo {

    private Class<?> clazz;
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
