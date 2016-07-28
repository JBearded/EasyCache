package com.proxy;

import com.annotation.CacheAnnotationScanner;
import com.annotation.ClassCacheAnnInfo;
import com.bean.BeanFactoryInterface;
import net.sf.cglib.proxy.Enhancer;

import java.util.List;

/**
 * @author 谢俊权
 * @create 2016/7/25 16:49
 */
public class CacheInterceptor {

    private BeanFactoryInterface beanFactory;

    public CacheInterceptor(BeanFactoryInterface beanFactory) {
        this.beanFactory = beanFactory;
    }

    public void run(String pack) throws Exception {
        List<ClassCacheAnnInfo> classCacheAnnInfoList =  CacheAnnotationScanner.scan(pack);
        for(ClassCacheAnnInfo classCacheAnnInfo : classCacheAnnInfoList){
            Class<?> clazz = classCacheAnnInfo.getClazz();
            Object object = createProxyBean(classCacheAnnInfo);
            beanFactory.set(clazz, object);
        }

    }

    private Object createProxyBean(ClassCacheAnnInfo info){
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(info.getClazz());
        CacheProxyHandler handler = new CacheProxyHandler(beanFactory, info);
        enhancer.setCallback(handler);
        Object result = enhancer.create();
        return result;
    }

}
