package com.ecache.annotation;

import com.ecache.utils.PackageScanner;

import java.lang.reflect.Method;
import java.util.*;

/**
 * 缓存注解的扫描器
 * @author 谢俊权
 * @create 2016/7/25 10:24
 */
public class CacheAnnotationScanner {

    /**
     * 扫描包名下所有带有缓存注解的类
     * @param pack 包名
     * @return 类注解信息列表
     */
    public static List<ClassCacheAnnInfo> scan(String pack){

        List<ClassCacheAnnInfo> result = new ArrayList<>();
        Set<Class<?>> classSet = PackageScanner.getClasses(pack);
        for(Class<?> clazz : classSet){
            List<MethodCacheAnnInfo> annList = new ArrayList<>();
            for(Method method : clazz.getDeclaredMethods()){
                LocalCache localCacheAn = method.getAnnotation(LocalCache.class);
                RemoteCache remoteCacheAn = method.getAnnotation(RemoteCache.class);
                if(localCacheAn != null || remoteCacheAn != null){
                    MethodCacheAnnInfo annInfo = new MethodCacheAnnInfo();
                    annInfo.setMethod(method);
                    if(localCacheAn != null){
                        annInfo.setCacheClazz(com.ecache.LocalCache.class);
                        annInfo.setKey(localCacheAn.key());
                        annInfo.setExpiredSeconds(localCacheAn.expire());
                        annInfo.setAvoidOverload(localCacheAn.avoidOverload());
                    }else if(remoteCacheAn != null){
                        annInfo.setCacheClazz(com.ecache.RemoteCache.class);
                        annInfo.setKey(remoteCacheAn.key());
                        annInfo.setExpiredSeconds(remoteCacheAn.expire());
                        annInfo.setAvoidOverload(remoteCacheAn.avoidOverload());
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
