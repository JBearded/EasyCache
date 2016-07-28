package com.bean;

import java.util.Map;

/**
 * @author 谢俊权
 * @create 2016/7/27 11:36
 */
public class BeanClassLoader extends ClassLoader{

    private final Map<String, byte[]> injectClassCode;

    public BeanClassLoader(Map<String, byte[]> injectClassCode) {
        super(Thread.currentThread().getContextClassLoader());
        this.injectClassCode = injectClassCode;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        byte[] code = injectClassCode.get(name);
        if(code != null){
            return super.defineClass(name, code, 0, code.length);
        }
        return super.loadClass(name);
    }

}
