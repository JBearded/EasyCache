package com.proxy;

import com.annotation.ClassCacheAnnInfo;
import com.annotation.MethodCacheAnnInfo;
import com.bean.BeanFactoryInterface;
import com.cache.AbstractCache;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 谢俊权
 * @create 2016/7/27 18:57
 */
public class CacheProxyHandler implements MethodInterceptor {

    private BeanFactoryInterface beanFactory;
    private ClassCacheAnnInfo classCacheAnnInfo;

    public CacheProxyHandler(BeanFactoryInterface beanFactory, ClassCacheAnnInfo annInfo) {
        this.beanFactory = beanFactory;
        this.classCacheAnnInfo = annInfo;
    }

    @Override
    public Object intercept(Object object, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {

        Object result = null;
        MethodCacheAnnInfo methodCacheAnnInfo = getMethodAnnInfo(method);
        if(methodCacheAnnInfo != null){
            String key = getCacheKey(methodCacheAnnInfo, object, method, args);
            AbstractCache cacheObject = getCacheObject(methodCacheAnnInfo);
            Object value = cacheObject.get(key, method.getReturnType());
            if(value != null){
                result = value;
            }else{
                result = methodProxy.invokeSuper(object, args);
                if(result != null){
                    int expireSeconds = methodCacheAnnInfo.getExpireTime();
                    cacheObject.set(key, result, expireSeconds);
                }
            }
        }else{
            result = methodProxy.invokeSuper(object, args);
        }
        return result;
    }

    private MethodCacheAnnInfo getMethodAnnInfo(Method method){
        for(MethodCacheAnnInfo info : classCacheAnnInfo.getMethodAnnInfoList()){
            if(method.equals(info.getMethod())){
                return info;
            }
        }
        return null;
    }

    private String getCacheKey(MethodCacheAnnInfo info, Object object, Method method, Object[] args){

        Class<?> clazz = classCacheAnnInfo.getClazz();
        String annotationKey = info.getKey();
        String defaultKey = getDefaultKey(clazz, method);
        StringBuilder keyBuilder = new StringBuilder(defaultKey);
        String regex = "\\$([1-9]{1})(\\.\\w+)?";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(annotationKey);
        while(matcher.find()){
            String item = matcher.group();
            Object keyValue = null;
            String index = item.substring(1,2);
            int paramIndex = Integer.valueOf(index);
            if(paramIndex <= args.length){
                int methodIndex = item.indexOf(".");
                if(methodIndex < 0){
                    keyValue = args[paramIndex - 1];
                }else{
                    String fieldName = item.substring(methodIndex + 1);
                    String getMethodName = "get" + fieldName.toUpperCase().substring(0,1) + fieldName.substring(1);
                    keyValue = getMethodValue(args[paramIndex - 1], getMethodName);
                }
                if(keyValue != null){
                    keyBuilder.append(keyValue);
                }
            }
        }
        return keyBuilder.toString();
    }

    private String getDefaultKey(Class<?> clazz, Method method){
        String objectName = clazz.getName().replace(".", "");
        String methodName = method.getName();
        return objectName + methodName;
    }

    private Object getMethodValue(Object object, String getMethodName) {

        Object value = null;
        try{
            Class<?> clazz = object.getClass();
            Method getMethod = clazz.getMethod(getMethodName, null);
            value = getMethod.invoke(object, null);
        }catch (Exception e){

        }
        return value;
    }

    private AbstractCache getCacheObject(MethodCacheAnnInfo info){
        Class<? extends AbstractCache> cacheClazz = info.getCacheClazz();
        AbstractCache cacehObject = beanFactory.get(cacheClazz);
        return cacehObject;
    }
}
