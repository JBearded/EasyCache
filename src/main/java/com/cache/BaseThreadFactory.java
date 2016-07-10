package com.cache;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程工厂
 *
 * @author 谢俊权
 * @create 2016/2/3 14:24
 */
public class BaseThreadFactory implements ThreadFactory{

    protected final AtomicInteger groupNumber = new AtomicInteger(1);
    protected final AtomicInteger threadNumber = new AtomicInteger(1);
    protected String threadNamePrefix;

    private ThreadGroup threadGroup;

    public BaseThreadFactory() {
        SecurityManager sm = System.getSecurityManager();
        threadGroup = (sm != null) ? sm.getThreadGroup() : Thread.currentThread().getThreadGroup();
        threadNamePrefix = "pool-" + groupNumber.getAndIncrement() + "-thread-";
    }

    public BaseThreadFactory(String name){
        SecurityManager sm = System.getSecurityManager();
        threadGroup = (sm != null) ? sm.getThreadGroup() : Thread.currentThread().getThreadGroup();
        threadNamePrefix = name + "-pool-" + groupNumber.getAndIncrement() + "-thread-";
    }


    @Override
    public Thread newThread(Runnable runnable) {
        String threadName = threadNamePrefix + threadNumber.getAndIncrement();
        Thread thread = new Thread(threadGroup, runnable, threadName, 0);
        if(thread.isDaemon()){
            thread.setDaemon(false);
        }
        if(thread.getPriority() != Thread.NORM_PRIORITY){
            thread.setPriority(Thread.NORM_PRIORITY);
        }
        return thread;
    }
}
