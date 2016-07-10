package com.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author 谢俊权
 * @create 2016/7/10 10:40
 */
public class Scheduler {

    private ScheduledExecutorService scheduledExecutorService;

    private Map<String, ScheduledFuture> scheduledFutureMap = new HashMap<>();

    public Scheduler(int corePoolSize){
        ThreadFactory threadFactory = new BaseThreadFactory("easy-cache");
        this.scheduledExecutorService = Executors.newScheduledThreadPool(corePoolSize, threadFactory);
    }

    public void run(String key, int delay, int interval, Runnable runnable){
        ScheduledFuture scheduledFutureTask = this.scheduledExecutorService.scheduleWithFixedDelay(runnable, delay, interval, TimeUnit.SECONDS);
        this.scheduledFutureMap.put(key, scheduledFutureTask);
    }

    public void run(String key, int interval, Runnable runnable){
        this.run(key, 0, interval, runnable);
    }

    public void cancel(String key){
        if(this.scheduledFutureMap.containsKey(key)){
            ScheduledFuture scheduledFutureTask = this.scheduledFutureMap.remove(key);
            scheduledFutureTask.cancel(true);
        }
    }
}
