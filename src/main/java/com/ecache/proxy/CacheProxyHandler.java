package com.ecache.proxy;

import com.ecache.CacheType;
import com.ecache.EasyCache;
import com.ecache.annotation.CacheAnnotationInfo;
import com.ecache.annotation.ClassCacheAnInfo;
import com.ecache.annotation.MethodCacheAnInfo;
import com.ecache.annotation.NullCacheInstance;
import com.ecache.bean.BeanFactoryInterface;
import com.ecache.exception.CacheKeyOutOfArgsException;
import com.ecache.exception.CacheObjectNotFoundException;
import com.ecache.exception.MissDefaultCacheException;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 谢俊权
 * @create 2016/7/27 18:57
 */
public class CacheProxyHandler implements MethodInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(CacheProxyHandler.class);

    /**
     * bean工厂, 用于获取容器中的对象
     */
    private BeanFactoryInterface beanFactory;

    /**
     * 类的EasyCache注解相关信息
     */
    private ClassCacheAnInfo classCacheAnInfo;

    public CacheProxyHandler(BeanFactoryInterface beanFactory, ClassCacheAnInfo annInfo) {
        this.beanFactory = beanFactory;
        this.classCacheAnInfo = annInfo;
    }

    @Override
    public Object intercept(Object object, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {

        MethodCacheAnInfo methodCacheAnInfo = getMethodAnnInfo(method);
        if(methodCacheAnInfo == null){
            return methodProxy.invokeSuper(object, args);
        }
        String key = getCacheKey(methodCacheAnInfo, args);
        Object result = getCacheValue(methodCacheAnInfo, key);
        if(result != null){
            return result;
        }
        result = methodProxy.invokeSuper(object, args);
        setCacheValue(methodCacheAnInfo, key, result);
        return result;
    }

    /**
     * 从类的多个注解方法中, 获取与调用方法相同的注解信息
     * @param method 被调用的方法
     * @return 方法注解信息
     */
    private MethodCacheAnInfo getMethodAnnInfo(Method method){
        for(MethodCacheAnInfo info : classCacheAnInfo.getMethodAnnInfoList()){
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
    private String getCacheKey(MethodCacheAnInfo info, Object[] args) {

        Class<?> clazz = classCacheAnInfo.getClazz();
        Method method = info.getMethod();
        String annotationKey = info.getKey();
        String prefixKey = getPrefixKey(clazz, method);
        if(annotationKey == null || "".equals(annotationKey.trim())){
            return prefixKey;
        }
        String cacheKey = new StringBuffer(prefixKey).append("_").append(annotationKey).toString();
        String regex = "[\\{]{0,1}\\$([1-9]{1})(\\.\\w+)?[\\}]{0,1}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(annotationKey);
        while(matcher.find()){
            String key = matcher.group();
            Object keyValue = getKeyValue(key, args);
            cacheKey = cacheKey.replace(key, keyValue.toString());
        }
        return cacheKey;
    }

    /**
     * 通过匹配到的key关键字($1,{$2.name}等), 获取其参数值
     * @param key
     * @return
     */
    private Object getKeyValue(String key, Object[] args){
        String item = key.replaceAll("[\\{|\\}]", "");
        String index = item.substring(1,2);
        int paramIndex = Integer.valueOf(index);
        if(paramIndex > args.length){
            throw new CacheKeyOutOfArgsException("cache key $number out of args");
        }
        int methodIndex = item.indexOf(".");
        if(methodIndex < 0){
            return args[paramIndex - 1];
        }
        String fieldName = item.substring(methodIndex + 1);
        Object paramObject = args[paramIndex - 1];
        return getFieldValue(fieldName, paramObject);
    }

    /**
     * 获取默认的缓存key
     * @param clazz 具有注解方法的类
     * @param method    注解方法
     * @return  默认缓存key
     */
    private String getPrefixKey(Class<?> clazz, Method method){
        String objectName = clazz.getName();
        String methodName = method.getName();
        return objectName + "." + methodName;
    }

    /**
     * 获取对象实例的属性值
     * @param fieldName 属性名称
     * @param object    对象实例
     * @return
     */
    private Object getFieldValue(String fieldName, Object object){
        Class<?> paramClass = object.getClass();
        try{
            Field field = paramClass.getDeclaredField(fieldName);
            if(!Modifier.isPublic(field.getModifiers())){
                field.setAccessible(true);
            }
            return field.get(object);
        }catch (NoSuchFieldException ne){
            logger.error("does not exist attribute {} in class {}", fieldName, paramClass.getName(), ne);
            throw new RuntimeException(ne);
        }catch (IllegalAccessException ae){
            logger.error("get field {} error in class {}", fieldName, paramClass.getName(), ae);
            throw new RuntimeException(ae);
        }
    }

    /**
     * 通过内部缓存获取缓存数据（内部缓存：@LocalCache和@RemoteCache注解用的缓存）
     * @param methodCacheInfo    方法缓存注解信息
     * @param key   方法缓存key
     * @return
     */
    private Object getCacheValue(
            MethodCacheAnInfo methodCacheInfo,
            String key){

        Class<? extends EasyCache> cacheClazz = methodCacheInfo.getCacheClazz();
        Class<? extends EasyCache> targetCacheClazz =
                (cacheClazz == null || cacheClazz.equals(NullCacheInstance.class))
                        ? getDefaultCacheInstance() : cacheClazz;
        EasyCache cacheObject = getCacheObject(targetCacheClazz);

        Method method = methodCacheInfo.getMethod();
        Type type = method.getGenericReturnType();
        ParameterizedType parameterizedType = (type instanceof ParameterizedType) ? (ParameterizedType) type : null;
        return (parameterizedType == null)
                ? cacheObject.get(key, (method.getReturnType()))
                : cacheObject.get(key, new CacheType(parameterizedType) {});
    }

    /**
     * 保存结果到缓存中
     * @param methodCacheInfo  方法注解信息
     * @param key   缓存key
     * @param value 缓存值
     * @return
     */
    private Object setCacheValue(MethodCacheAnInfo methodCacheInfo, String key, Object value){

        Class<? extends EasyCache> cacheClazz = methodCacheInfo.getCacheClazz();
        Class<? extends EasyCache> targetCacheClazz =
                (cacheClazz == null || cacheClazz.equals(NullCacheInstance.class))
                        ? getDefaultCacheInstance() : cacheClazz;
        int expiredSeconds = methodCacheInfo.getExpiredSeconds();
        EasyCache cacheObject = getCacheObject(targetCacheClazz);
        return cacheObject.set(key, value, expiredSeconds);
    }

    private Class<? extends EasyCache> getDefaultCacheInstance(){
        CacheAnnotationInfo info = CacheAnnotationInfo.getInstance();
        List defaultCacheList = info.getDefaultCacheList();
        if(defaultCacheList.isEmpty()){
            throw new MissDefaultCacheException("must set a default cache instance using @DefaultCache");
        }
        return (Class<? extends EasyCache>) defaultCacheList.get(0);
    }

    /**
     * 获取容器中的缓存对象
     * @param cacheClazz    缓存对象类型
     * @return
     */
    private EasyCache getCacheObject(Class<? extends EasyCache> cacheClazz){
        EasyCache cacheObject = beanFactory.get(cacheClazz);
        if(cacheObject == null){
            String errorMessage = "failed to get inner cache object, class:" + cacheClazz;
            throw new CacheObjectNotFoundException(errorMessage);
        }
        return cacheObject;
    }

}
