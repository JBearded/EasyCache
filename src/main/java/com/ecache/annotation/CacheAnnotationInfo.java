package com.ecache.annotation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author xiejunquan
 * @create 2016/12/14 14:38
 */
public class CacheAnnotationInfo {

    List<ClassCacheAnInfo> cacheAnInfoList = new ArrayList<>();
    List<Class<?>> defaultCacheList = new ArrayList<>();

    private static class CacheAnnotationInfoHolder{
        private static CacheAnnotationInfo info = new CacheAnnotationInfo();
    }

    private CacheAnnotationInfo(){
    }

    public static CacheAnnotationInfo getInstance(){
        return CacheAnnotationInfoHolder.info;
    }

    public List<ClassCacheAnInfo> getCacheAnInfoList() {
        return Collections.unmodifiableList(cacheAnInfoList);
    }

    public List<Class<?>> getDefaultCacheList() {
        return Collections.unmodifiableList(defaultCacheList);
    }
}
