package com.ecache.proxy;

import com.alibaba.fastjson.JSON;
import com.ecache.AbstractCache;
import com.ecache.CacheInterface;
import com.ecache.CacheType;
import com.ecache.RemoteCache;
import com.ecache.annotation.ClassCacheAnnInfo;
import com.ecache.annotation.MethodCacheAnnInfo;
import com.ecache.bean.BeanFactoryInterface;
import com.ecache.exception.CacheKeyOutOfArgsException;
import com.ecache.exception.InnerCacheObjectNotFoundException;
import com.ecache.exception.OuterCacheObjectNotFoundException;
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

    /**
     * bean工厂, 用于获取容器中的对象
     */
    private BeanFactoryInterface beanFactory;

    /**
     * 类的EasyCache注解相关信息
     */
    private ClassCacheAnnInfo classCacheAnnInfo;

    public CacheProxyHandler(BeanFactoryInterface beanFactory, ClassCacheAnnInfo annInfo) {
        this.beanFactory = beanFactory;
        this.classCacheAnnInfo = annInfo;
    }

    @Override
    public Object intercept(Object object, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {

        MethodCacheAnnInfo methodCacheAnnInfo = getMethodAnnInfo(method);
        if(methodCacheAnnInfo == null){
            return methodProxy.invokeSuper(object, args);
        }
        String key = getCacheKey(methodCacheAnnInfo, args);
        Object result = getCacheValue(methodCacheAnnInfo, key);
        if(result != null){
            return result;
        }
        result = methodProxy.invokeSuper(object, args);
        setCacheValue(methodCacheAnnInfo, key, result);
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
        }catch (IllegalAccessException ae){
            logger.error("get field {} error in class {}", fieldName, paramClass.getName(), ae);
        }
        return null;
    }

    /**
     * 获取缓存值
     * @param methodCacheAnnInfo    方法缓存注解信息
     * @param key   方法缓存的key
     * @return
     */
    private Object getCacheValue(MethodCacheAnnInfo methodCacheAnnInfo, String key){
        if(methodCacheAnnInfo.isOuterCache()){
            return getOuterCacheValue(methodCacheAnnInfo, key);
        }else if(methodCacheAnnInfo.isInnerCache()){
            return getInnerCacheValue(methodCacheAnnInfo, key);
        }
        return null;
    }

    /**
     * 通过内部缓存获取缓存数据（内部缓存：@LocalCache和@RemoteCache注解用的缓存）
     * @param methodCacheInfo    方法缓存注解信息
     * @param key   方法缓存key
     * @return
     */
    private Object getInnerCacheValue(
            MethodCacheAnnInfo methodCacheInfo,
            String key){

        Class<? extends AbstractCache> cacheClazz = methodCacheInfo.getInnerCacheClazz();
        AbstractCache cacheObject = getInnerCacheObject(cacheClazz);

        Method method = methodCacheInfo.getMethod();
        Type type = method.getGenericReturnType();
        ParameterizedType parameterizedType = (type instanceof ParameterizedType) ? (ParameterizedType) type : null;
        if(parameterizedType != null && cacheObject instanceof RemoteCache){
            RemoteCache remoteCache = (RemoteCache) cacheObject;
            return remoteCache.get(key, new CacheType(parameterizedType) {});
        }
        Class<Object> returnClazz = (Class<Object>) ((parameterizedType != null) ? parameterizedType.getRawType() : type);
        return cacheObject.get(key, returnClazz);
    }

    /**
     * 通过外部缓存获取缓存数据（外部缓存：@Cache注解用的缓存）
     * @param methodCacheAnnInfo    方法缓存注解信息
     * @param key   方法缓存key
     * @return
     */
    private Object getOuterCacheValue(MethodCacheAnnInfo methodCacheAnnInfo, String key){
        Class<? extends CacheInterface> cacheClazz = methodCacheAnnInfo.getOuterCacheClazz();
        String id = methodCacheAnnInfo.getId();
        CacheInterface cacheObject = getOuterCacheObject(cacheClazz, id);

        String value = cacheObject.get(key);
        if(value != null){
            Method method = methodCacheAnnInfo.getMethod();
            Type type = method.getGenericReturnType();
            if(!(type instanceof ParameterizedType)){
                return JSON.parseObject(value, method.getReturnType());
            }
            ParameterizedType parameterizedType = (ParameterizedType) type;
            CacheType cacheType = new CacheType(parameterizedType){};
            return JSON.parseObject(value, cacheType.actualType);
        }
        return null;
    }

    /**
     * 保存结果到缓存中
     * @param info  方法注解信息
     * @param key   缓存key
     * @param value 缓存值
     * @return
     */
    private Object setCacheValue(MethodCacheAnnInfo info, String key, Object value){
        int expiredSeconds = info.getExpiredSeconds();
        if(info.isInnerCache()){
            AbstractCache cacheObject = getInnerCacheObject(info.getInnerCacheClazz());
            cacheObject.set(key, value, expiredSeconds);
        }else if(info.isOuterCache()){
            CacheInterface cacheObject = getOuterCacheObject(info.getOuterCacheClazz(), info.getId());
            cacheObject.set(key, JSON.toJSONString(value), expiredSeconds);
        }
        return value;
    }

    /**
     * 获取容器中的外部缓存对象(@Cache中使用到的缓存对像)
     * @param cacheClazz    缓存对象类型
     * @param id    缓存对象的id
     * @return
     */
    private CacheInterface getOuterCacheObject(Class<? extends CacheInterface> cacheClazz, String id){
        CacheInterface cacheObject = (CacheInterface) ((id == null || "".equals(id.trim()))
                        ? beanFactory.get(cacheClazz)
                        : beanFactory.get(cacheClazz, id));

        if(cacheObject == null){
            String errorMessage = "failed to get outer cache object, class:" + cacheClazz;
            throw new OuterCacheObjectNotFoundException(errorMessage);
        }
        return cacheObject;
    }

    /**
     * 获取容器中的外部缓存对象(@RemoteCache和@LocalCache中使用到的缓存对像)
     * @param cacheClazz    缓存对象类型
     * @return
     */
    private AbstractCache getInnerCacheObject(Class<? extends AbstractCache> cacheClazz){
        AbstractCache cacheObject = beanFactory.get(cacheClazz);
        if(cacheObject == null){
            String errorMessage = "failed to get inner cache object, class:" + cacheClazz;
            throw new InnerCacheObjectNotFoundException(errorMessage);
        }
        return cacheObject;
    }

}
