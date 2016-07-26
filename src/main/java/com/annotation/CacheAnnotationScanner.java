package com.annotation;

import com.utils.PackageScanner;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author 谢俊权
 * @create 2016/7/25 10:24
 */
public class CacheAnnotationScanner {

    public static Map<String, CacheAnnotationInfo> scan(String pkage){

        Map<String, CacheAnnotationInfo> result = new HashMap<>();
        Set<Class<?>> classSet = PackageScanner.getClasses(pkage);
        for(Class<?> clazz : classSet){
            for(Method method : clazz.getDeclaredMethods()){
                LocalCache localCacheAn = method.getAnnotation(LocalCache.class);
                RemoteCache remoteCacheAn = method.getAnnotation(RemoteCache.class);
                if(localCacheAn != null || remoteCacheAn != null){
                    CacheAnnotationInfo info = new CacheAnnotationInfo();
                    info.setClazz(clazz);
                    info.setMethod(method);
                    if(localCacheAn != null){
                        info.setCacheClazz(com.cache.LocalCache.class);
                        info.setKey(localCacheAn.key());
                        info.setExpireTime(localCacheAn.expire());
                    }else if(remoteCacheAn != null){
                        info.setCacheClazz(com.cache.RemoteCache.class);
                        info.setKey(remoteCacheAn.key());
                        info.setExpireTime(remoteCacheAn.expire());
                    }
                    String key = clazz.getName() + "." + method.getName();
                    result.put(key, info);
                }
            }
        }
        return result;
    }


}
