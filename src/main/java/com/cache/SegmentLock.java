package com.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author 谢俊权
 * @create 2016/7/29 17:18
 */
public class SegmentLock {

    private int segments = 16;
    private final Map<Integer, ReentrantLock> lockMap = new HashMap<>();

    public SegmentLock(int count, boolean fair) {
        init(count, fair);
    }

    private void init(int count, boolean fair){
        if(count > segments){
            segments = count;
        }
        for(int i = 0; i < segments; i++){
            lockMap.put(Integer.valueOf(i), new ReentrantLock(fair));
        }
    }

    public <T> void lock(T key){
        int index = getKeyIndex(key);
        ReentrantLock lock = lockMap.get(index);
        lock.lock();
    }

    public <T> void unlock(T key){
        int index = getKeyIndex(key);
        ReentrantLock lock = lockMap.get(index);
        lock.unlock();
    }

    private <T> int getKeyIndex(T key){
        return (key.hashCode() >>> 1) % segments;    //无符号右移一位变整数
    }
}
