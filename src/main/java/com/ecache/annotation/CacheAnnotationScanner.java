package com.ecache.annotation;

import com.ecache.*;
import com.ecache.exception.MissDefaultCacheException;
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
    public static void scan(String pack){

        List<ClassCacheAnInfo> cacheAnInfoList = new ArrayList<>();
        List<Class<?>> defaultCacheList = new ArrayList<>();
        Set<Class<?>> classSet = PackageScanner.getClasses(pack);
        boolean useDefaultCache = false;
        for(Class<?> clazz : classSet){
            DefaultCache defaultCache = clazz.getAnnotation(DefaultCache.class);
            if(defaultCache != null && EasyCache.class.isAssignableFrom(clazz)){
                defaultCacheList.add(clazz);
                logger.info("default cache instance:{}", clazz.getName());
            }
            List<MethodCacheAnInfo> annList = new ArrayList<>();
            for(Method method : clazz.getDeclaredMethods()){
                MethodCacheAnInfo cacheAnInfo = getCache(method);
                if(cacheAnInfo != null){
                    annList.add(cacheAnInfo);
                    Class<? extends EasyCache> instance = cacheAnInfo.getCacheClazz();
                    boolean haveDefaultCache = instance == null || instance.equals(NullCacheInstance.class);
                    useDefaultCache = (haveDefaultCache) ? true : useDefaultCache;
                    logger.info("@Cache info:{}.{}", clazz.getName(), cacheAnInfo);
                }
                MethodCacheAnInfo localCacheAnInfo = getLocalCache(method);
                if(localCacheAnInfo != null){
                    annList.add(localCacheAnInfo);
                    logger.info("@LocalCache info:{}.{}", clazz.getName(), localCacheAnInfo);
                }
            }
            if(!annList.isEmpty()){
                ClassCacheAnInfo classAnnInfo = new ClassCacheAnInfo(clazz, annList);
                cacheAnInfoList.add(classAnnInfo);
            }
        }
        if(defaultCacheList.isEmpty() && useDefaultCache){
            throw new MissDefaultCacheException("must set a default cache instance using @DefaultCache");
        }
        CacheAnnotationInfo info = CacheAnnotationInfo.getInstance();
        info.cacheAnInfoList.addAll(cacheAnInfoList);
        info.defaultCacheList.addAll(defaultCacheList);
    }


    private static MethodCacheAnInfo getCache(Method method){
        Cache cacheAn = method.getAnnotation(Cache.class);
        if(cacheAn == null){
            return null;
        }
        MethodCacheAnInfo cacheAnnInfo = new MethodCacheAnInfo.Builder()
                .method(method)
                .cacheClazz(cacheAn.instance())
                .key(cacheAn.key())
                .expiredSeconds(cacheAn.expired())
                .buil();
        return cacheAnnInfo;
    }

    private static MethodCacheAnInfo getLocalCache(Method method){
        LocalCache cacheAn = method.getAnnotation(LocalCache.class);
        if(cacheAn == null){
            return null;
        }
        MethodCacheAnInfo cacheAnnInfo = new MethodCacheAnInfo.Builder()
                .method(method)
                .cacheClazz(com.ecache.LocalCache.class)
                .key(cacheAn.key())
                .expiredSeconds(cacheAn.expired())
                .buil();
        return cacheAnnInfo;
    }

}
