package com.ecache.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author 谢俊权
 * @create 2016/7/29 19:05
 */
public class HashLock {

    private boolean fair = false;
    private final SegmentLock segmentLock;
    private final Map<Object, LockInfo> lockMap;

    public HashLock(int segments, boolean fair) {
        this.fair = fair;
        this.segmentLock = new SegmentLock(segments, fair);
        this.lockMap = new ConcurrentHashMap<>();
    }

    public void lock(Object key){
        LockInfo lockInfo = null;
        segmentLock.lock(key);
        try{
            lockInfo = lockMap.get(key);
            if(lockInfo == null){
                lockInfo = new LockInfo(fair);
                lockMap.put(key, lockInfo);
            }else{
                lockInfo.increment();
            }
        }finally {
            segmentLock.unlock(key);
        }
        lockInfo.lock();
    }

    public void unlock(Object key){
        LockInfo lockInfo = lockMap.get(key);
        if(lockInfo != null){
            if(lockInfo.needRelease()){
                segmentLock.lock(key);
                try{
                    if(lockInfo.needRelease()){
                        lockMap.remove(key);
                    }
                }finally {
                    segmentLock.unlock(key);
                }
            }
            lockInfo.decrement();
            lockInfo.unlock();
        }
    }

    private class LockInfo{
        private ReentrantLock lock;
        private AtomicInteger count = new AtomicInteger(1);

        public LockInfo(boolean fair) {
            this.lock = new ReentrantLock(fair);
        }

        public void lock(){
            this.lock.lock();
        }

        public void unlock(){
            this.lock.unlock();
        }

        public void increment(){
            count.incrementAndGet();
        }

        public void decrement(){
            count.decrementAndGet();
        }

        public boolean needRelease(){
            return (count.get() == 1);
        }
    }
}
