package com.ecache.proxy;

import com.ecache.annotation.ClassCacheAnnInfo;
import com.ecache.annotation.MethodCacheAnnInfo;
import com.ecache.bean.BeanFactoryInterface;
import com.ecache.AbstractCache;
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
            String key = getCacheKey(methodCacheAnnInfo, args);
            AbstractCache cacheObject = getCacheObject(methodCacheAnnInfo);
            Object value = cacheObject.get(key, method.getReturnType());
            if(value == null){
                if(methodCacheAnnInfo.isAvoidOverload()){
                    cacheObject.lock(key);
                    try{
                        value = cacheObject.get(key, method.getReturnType());
                        if(value == null){
                            result = methodProxy.invokeSuper(object, args);
                            if(result != null){
                                int expiredSeconds = methodCacheAnnInfo.getExpiredSeconds();
                                cacheObject.set(key, result, expiredSeconds);
                            }
                        }else {
                            result = value;
                        }
                    }finally {
                        cacheObject.unlock(key);
                    }
                }else{
                    result = methodProxy.invokeSuper(object, args);
                    if(result != null){
                        int expiredSeconds = methodCacheAnnInfo.getExpiredSeconds();
                        cacheObject.set(key, result, expiredSeconds);
                    }
                }
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
    private String getCacheKey(MethodCacheAnnInfo info, Object[] args){

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
                    String getMethodName = "get" + fieldName.toUpperCase().substring(0,1) + fieldName.substring(1);
                    keyValue = getArgMethodValue(args[paramIndex - 1], getMethodName);
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
            Method getMethod = clazz.getMethod(getMethodName, null);
            value = getMethod.invoke(object, null);
        }catch (Exception e){

        }
        return value;
    }

    /**
     * 从Bean容器中获取相对应的缓存实例, LocalCache或者RemoteCache实例
     * @param info  方法注解信息
     * @return
     */
    private AbstractCache getCacheObject(MethodCacheAnnInfo info){
        Class<? extends AbstractCache> cacheClazz = info.getCacheClazz();
        AbstractCache cacehObject = beanFactory.get(cacheClazz);
        return cacehObject;
    }
}
