package com.annotation;

import com.utils.PackageScanner;

import java.lang.reflect.Method;
import java.util.*;

/**
 * @author 谢俊权
 * @create 2016/7/25 10:24
 */
public class CacheAnnotationScanner {

    public static List<ClassCacheAnnInfo> scan(String pkage){

        List<ClassCacheAnnInfo> result = new ArrayList<>();
        Set<Class<?>> classSet = PackageScanner.getClasses(pkage);
        for(Class<?> clazz : classSet){
            List<MethodCacheAnnInfo> annList = new ArrayList<>();
            for(Method method : clazz.getDeclaredMethods()){
                LocalCache localCacheAn = method.getAnnotation(LocalCache.class);
                RemoteCache remoteCacheAn = method.getAnnotation(RemoteCache.class);
                if(localCacheAn != null || remoteCacheAn != null){
                    MethodCacheAnnInfo annInfo = new MethodCacheAnnInfo();
                    annInfo.setMethod(method);
                    if(localCacheAn != null){
                        annInfo.setCacheClazz(com.cache.LocalCache.class);
                        annInfo.setKey(localCacheAn.key());
                        annInfo.setExpireTime(localCacheAn.expire());
                    }else if(remoteCacheAn != null){
                        annInfo.setCacheClazz(com.cache.RemoteCache.class);
                        annInfo.setKey(remoteCacheAn.key());
                        annInfo.setExpireTime(remoteCacheAn.expire());
                    }
                    annList.add(annInfo);
                }
            }
            if(!annList.isEmpty()){
                ClassCacheAnnInfo classAnnInfo = new ClassCacheAnnInfo(clazz, annList);
                result.add(classAnnInfo);
            }
        }
        return result;
    }


}
