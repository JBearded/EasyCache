package com.ecache.proxy;

import com.ecache.annotation.CacheAnnotationScanner;
import com.ecache.annotation.ClassCacheAnnInfo;
import com.ecache.bean.BeanFactoryInterface;
import com.ecache.bean.InjectorInterface;
import net.sf.cglib.proxy.Enhancer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author 谢俊权
 * @create 2016/7/25 16:49
 */
public class CacheInterceptor {

    /**
     * Bean工厂接口, 用于外部加载代理的缓存bean
     */
    private BeanFactoryInterface beanFactory;

    /**
     * Bean注入器接口, 用于外部给代理的缓存bean注入其他容器中的属性
     */
    private InjectorInterface injectorInterface;


    public CacheInterceptor(BeanFactoryInterface beanFactory) {
        this.beanFactory = beanFactory;
    }

    public CacheInterceptor(BeanFactoryInterface beanFactory, InjectorInterface injectorInterface) {
        this.beanFactory = beanFactory;
        this.injectorInterface = injectorInterface;
    }

    /**
     * 扫描包名下的有LocalCache或者RemoteCache注解的类方法, 并将缓存代理类存入Bean容器
     * @param pack 需要扫描的包名
     */
    public void run(String pack){
        Map<Class<?>, Object> proxyBeanMap = new HashMap<>();
        List<ClassCacheAnnInfo> classCacheAnnInfoList =  CacheAnnotationScanner.scan(pack);
        for(ClassCacheAnnInfo classCacheAnnInfo : classCacheAnnInfoList){
            Class<?> clazz = classCacheAnnInfo.getClazz();
            Object object = createProxyBean(classCacheAnnInfo);
            beanFactory.set(clazz, object);
            proxyBeanMap.put(clazz, object);
        }
        inject(proxyBeanMap);
    }

    /**
     * cglib生成代理类对象
     * @param info 注解信息
     * @return  代理类对象
     */
    private Object createProxyBean(ClassCacheAnnInfo info){
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(info.getClazz());
        CacheProxyHandler handler = new CacheProxyHandler(beanFactory, info);
        enhancer.setCallback(handler);
        Object result = enhancer.create();
        return result;
    }

    /**
     * 如果有传入注入器类, 则进行代理类的属性注入
     * @param proxyBeanMap
     */
    private void inject(Map<Class<?>, Object> proxyBeanMap){
        if(injectorInterface != null){
            Iterator<Class<?>> it = proxyBeanMap.keySet().iterator();
            while(it.hasNext()){
                Class<?> clazz = it.next();
                Object object = proxyBeanMap.get(clazz);
                injectorInterface.doInject(object);
            }
        }
    }

}
