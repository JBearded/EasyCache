package com.ecache;

import java.util.concurrent.*;

/**
 * @author 谢俊权
 * @create 2016/7/10 10:40
 */
public class Scheduler {

    private ScheduledExecutorService scheduledExecutorService;

    private ConcurrentMap<String, ScheduledFuture> scheduledFutureMap = new ConcurrentHashMap<>();

    public Scheduler(int corePoolSize){
        ThreadFactory threadFactory = new BaseThreadFactory("easy-cache");
        this.scheduledExecutorService = Executors.newScheduledThreadPool(corePoolSize, threadFactory);
    }

    /**
     * 延迟一段时间后执行, 并重复在间隔时间后执行
     * @param key 任务标识, 可用来取消任务
     * @param delaySeconds 一开始的延迟时间, =0即马上执行
     * @param intervalSeconds  重复间隔时间, 必须>0
     * @param runnable
     */
    public void run(String key, int delaySeconds, int intervalSeconds, Runnable runnable){
        ScheduledFuture scheduledFutureTask = this.scheduledExecutorService.scheduleWithFixedDelay(runnable, delaySeconds, intervalSeconds, TimeUnit.SECONDS);
        this.scheduledFutureMap.put(key, scheduledFutureTask);
    }

    public void cancel(String key){
        if(this.scheduledFutureMap.containsKey(key)){
            ScheduledFuture scheduledFutureTask = this.scheduledFutureMap.remove(key);
            scheduledFutureTask.cancel(true);
        }
    }
}
