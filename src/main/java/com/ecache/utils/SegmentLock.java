package com.ecache.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author 谢俊权
 * @create 2016/7/29 17:18
 */
public class SegmentLock {

    private int MIN_SEGMENTS = 32;
    private int MAX_SEGMENTS = 1024;
    private int segments;
    private final Map<Integer, ReentrantLock> lockMap = new HashMap<>();

    public SegmentLock(int count, boolean fair) {
        init(count, fair);
    }

    private void init(int count, boolean fair){
        segments = (count < MIN_SEGMENTS) ? MIN_SEGMENTS : ((count > MAX_SEGMENTS) ? MAX_SEGMENTS : count);
        for(int i = 0; i < segments; i++){
            lockMap.put(Integer.valueOf(i), new ReentrantLock(fair));
        }
    }

    public void lock(Object key){
        int index = getKeyIndex(key);
        ReentrantLock lock = lockMap.get(index);
        lock.lock();
    }

    public void unlock(Object key){
        int index = getKeyIndex(key);
        ReentrantLock lock = lockMap.get(index);
        lock.unlock();
    }

    private int getKeyIndex(Object key){
        return (key.hashCode() >>> 1) % segments;    //无符号右移一位变整数
    }
}
