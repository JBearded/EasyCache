package com.ecache.proxy;

import com.alibaba.fastjson.JSON;
import com.ecache.AbstractCache;
import com.ecache.CacheInterface;
import com.ecache.CacheType;
import com.ecache.RemoteCache;
import com.ecache.annotation.ClassCacheAnnInfo;
import com.ecache.annotation.MethodCacheAnnInfo;
import com.ecache.bean.BeanFactoryInterface;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 谢俊权
 * @create 2016/7/27 18:57
 */
public class CacheProxyHandler implements MethodInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(CacheProxyHandler.class);

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
            String key = getCacheKey(methodCacheAnnInfo, args);
            Object value = getCacheValue(methodCacheAnnInfo, key);
            if(value == null){
                result = loadValue(methodCacheAnnInfo, key, object, args, methodProxy);
            }else {
                result = value;
            }
        }else{
            result = methodProxy.invokeSuper(object, args);
        }
        return result;
    }

    /**
     * 从类的多个注解方法中, 获取与调用方法相同的注解信息
     * @param method 被调用的方法
     * @return 方法注解信息
     */
    private MethodCacheAnnInfo getMethodAnnInfo(Method method){
        for(MethodCacheAnnInfo info : classCacheAnnInfo.getMethodAnnInfoList()){
            if(method.equals(info.getMethod())){
                return info;
            }
        }
        return null;
    }

    /**
     * 从注解key中获取到缓存需要的key值
     * @param info  方法注解信息
     * @param args  方法参数数组
     * @return  缓存key
     */
    private String getCacheKey(MethodCacheAnnInfo info, Object[] args) {

        Class<?> clazz = classCacheAnnInfo.getClazz();
        Method method = info.getMethod();
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
                    Object paramObject = args[paramIndex - 1];
                    Class<?> paramClass = paramObject.getClass();
                    try{
                        Field field = paramClass.getDeclaredField(fieldName);
                        if(!Modifier.isPublic(field.getModifiers())){
                            field.setAccessible(true);
                        }
                        keyValue = field.get(paramObject);
                    }catch (NoSuchFieldException ne){
                        logger.error("does not exist attribute {} in class {}", fieldName, paramClass.getName(), ne);
                    }catch (IllegalAccessException ae){
                        logger.error("get field {} error in class {}", fieldName, paramClass.getName(), ae);
                    }
                }
                if(keyValue != null){
                    keyBuilder.append("|").append(keyValue);
                }
            }
        }
        return keyBuilder.toString();
    }

    /**
     * 获取默认的缓存key, 不管注解key如何, 都会有默认ClassName + | + MethodName的key值
     * @param clazz 具有注解方法的类
     * @param method    注解方法
     * @return  默认缓存key
     */
    private String getDefaultKey(Class<?> clazz, Method method){
        String objectName = clazz.getName().replace(".", "|");
        String methodName = method.getName();
        return objectName +"|"+ methodName;
    }


    /**
     * 获取参数bean的属性值. 如果参数是一个bean, 那么通过此方法反射调用其属性值
     * @param object    参数bean对象
     * @param getMethodName 参数bean的getter方法名称
     * @return
     */
    private Object getArgMethodValue(Object object, String getMethodName) {

        Object value = null;
        try{
            Class<?> clazz = object.getClass();
            Method getMethod = clazz.getMethod(getMethodName, (Class<?>) null);
            value = getMethod.invoke(object, (Object) null);
        }catch (Exception e){

        }
        return value;
    }


    private Object getCacheValue(MethodCacheAnnInfo info, String key){
        Method method = info.getMethod();
        Type type = method.getGenericReturnType();
        if(info.isInnerCache()){
            Class<? extends AbstractCache> cacheClazz = info.getInnerCacheClazz();
            AbstractCache cacheObject = beanFactory.get(cacheClazz);
            if(cacheObject != null){
                if(type instanceof ParameterizedType && cacheObject instanceof RemoteCache){
                    RemoteCache remoteCache = (RemoteCache) cacheObject;
                    ParameterizedType parameterizedType = (ParameterizedType) type;
                    return remoteCache.get(key, new CacheType(parameterizedType){});
                }else{
                    return cacheObject.get(key, method.getReturnType());
                }
            }
        }else if(info.isOuterCache()){
            CacheInterface cacheObject = null;
            Class<? extends CacheInterface> cacheClazz = info.getOuterCacheClazz();
            String id = info.getId();
            if(id == null || "".equals(id)){
                cacheObject = beanFactory.get(cacheClazz);
            }else{
                cacheObject = beanFactory.get(cacheClazz, id);
            }
            if(cacheObject != null){
                String value = cacheObject.get(key);
                if(value != null){
                    if(type instanceof ParameterizedType){
                        ParameterizedType parameterizedType = (ParameterizedType) type;
                        CacheType cacheType = new CacheType(parameterizedType){};
                        return JSON.parseObject(value, cacheType.type);
                    }else{
                        return JSON.parseObject(value, method.getReturnType());
                    }
                }
            }
        }
        return null;
    }

    private Object setCacheValue(MethodCacheAnnInfo info, String key, Object value){
        int expiredSeconds = info.getExpiredSeconds();
        if(info.isInnerCache()){
            Class<? extends AbstractCache> cacheClazz = info.getInnerCacheClazz();
            AbstractCache cacheObject = beanFactory.get(cacheClazz);
            if(cacheObject != null){
                cacheObject.set(key, value, expiredSeconds);
            }
        }else if(info.isOuterCache()){
            CacheInterface cacheObject = null;
            Class<? extends CacheInterface> cacheClazz = info.getOuterCacheClazz();
            String id = info.getId();
            if(id == null || "".equals(id)){
                cacheObject = beanFactory.get(cacheClazz);
            }else{
                cacheObject = beanFactory.get(cacheClazz, id);
            }
            if(cacheObject != null){
                cacheObject.set(key, JSON.toJSONString(value), expiredSeconds);
            }
        }
        return value;
    }

    private Object loadValue(MethodCacheAnnInfo methodCacheAnnInfo, String key, Object object, Object[] args, MethodProxy methodProxy) throws Throwable {
        Object result = null;
        if(methodCacheAnnInfo.isInnerCache() && methodCacheAnnInfo.getInnerCacheClazz().equals(RemoteCache.class)){
            Class<? extends AbstractCache> cacheClazz = methodCacheAnnInfo.getInnerCacheClazz();
            AbstractCache cacheObject = beanFactory.get(cacheClazz);
            if(methodCacheAnnInfo.isAvoidOverload()){
                cacheObject.lock(key);
                try{
                    result = getCacheValue(methodCacheAnnInfo, key);
                    if(result == null){
                        result = loadValueDirectly(methodCacheAnnInfo, key, object, methodProxy, args);
                    }
                }finally {
                    cacheObject.unlock(key);
                }
            }else{
                result = loadValueDirectly(methodCacheAnnInfo, key, object, methodProxy, args);
            }
        }else{
            result = loadValueDirectly(methodCacheAnnInfo, key, object, methodProxy, args);
        }
        return result;
    }

    private Object loadValueDirectly(MethodCacheAnnInfo methodCacheAnnInfo, String key, Object object, MethodProxy methodProxy, Object[] args) throws Throwable {
        Object result = methodProxy.invokeSuper(object, args);
        if(result != null){
            setCacheValue(methodCacheAnnInfo, key, result);
        }
        return result;
    }
}
