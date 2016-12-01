package com.ecache.annotation;

import com.ecache.LocalCache;
import com.ecache.RemoteCache;
import com.ecache.utils.PackageScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 缓存注解的扫描器
 * @author 谢俊权
 * @create 2016/7/25 10:24
 */
public class CacheAnnotationScanner {

    private static final Logger logger = LoggerFactory.getLogger(CacheAnnotationScanner.class);

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
                com.ecache.annotation.LocalCache localCacheAn = method.getAnnotation(com.ecache.annotation.LocalCache.class);
                com.ecache.annotation.RemoteCache remoteCacheAn = method.getAnnotation(com.ecache.annotation.RemoteCache.class);
                Cache cacheAn = method.getAnnotation(Cache.class);
                MethodCacheAnnInfo methodCacheAnnInfo = null;
                if(cacheAn != null){
                    methodCacheAnnInfo = getCache(method, cacheAn);
                }else if(remoteCacheAn != null){
                    methodCacheAnnInfo = getRemoteCache(method, remoteCacheAn);
                }else if(localCacheAn != null){
                    methodCacheAnnInfo = getLocalCache(method, localCacheAn);
                }
                if(methodCacheAnnInfo != null){
                    logger.info("scan class, cacheInfo:{}", methodCacheAnnInfo.toString());
                    annList.add(methodCacheAnnInfo);
                }
            }
            if(!annList.isEmpty()){
                ClassCacheAnnInfo classAnnInfo = new ClassCacheAnnInfo(clazz, annList);
                result.add(classAnnInfo);
            }
        }
        return result;
    }

    private static MethodCacheAnnInfo getLocalCache(Method method, com.ecache.annotation.LocalCache localCacheAn){
        MethodCacheAnnInfo cacheAnnInfo = new MethodCacheAnnInfo.Builder()
                .method(method)
                .innerCacheClazz(LocalCache.class)
                .key(localCacheAn.key())
                .expiredSeconds(localCacheAn.expire())
                .avoidOverload(false)
                .buil();
        return cacheAnnInfo;
    }

    private static MethodCacheAnnInfo getRemoteCache(Method method, com.ecache.annotation.RemoteCache remoteCacheAn){
        MethodCacheAnnInfo cacheAnnInfo = new MethodCacheAnnInfo.Builder()
                .method(method)
                .innerCacheClazz(RemoteCache.class)
                .key(remoteCacheAn.key())
                .expiredSeconds(remoteCacheAn.expire())
                .avoidOverload(remoteCacheAn.avoidOverload())
                .buil();
        return cacheAnnInfo;
    }

    private static MethodCacheAnnInfo getCache(Method method, Cache cacheAn){
        MethodCacheAnnInfo cacheAnnInfo = new MethodCacheAnnInfo.Builder()
                .method(method)
                .outerCacheClazz(cacheAn.instance())
                .id(cacheAn.id())
                .key(cacheAn.key())
                .expiredSeconds(cacheAn.expire())
                .buil();
        return cacheAnnInfo;
    }


}
