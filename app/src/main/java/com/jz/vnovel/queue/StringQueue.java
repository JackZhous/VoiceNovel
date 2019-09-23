package com.jz.vnovel.queue;

import android.util.Log;

import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 字符小说队列
 */
public class StringQueue {

    LinkedList<String> queue;
    //最长有40条字符数据
    int maxLen = 30;
    Lock lock;
    int currentSize;
    Condition condition;

    boolean flush = false;

    public StringQueue(int maxLen) {
        this.maxLen = maxLen;
        queue = new LinkedList<>();
        lock = new ReentrantLock();
        currentSize = 0;
        condition = lock.newCondition();
    }

    public void pushMsg(String msg){
        lock.lock();

        try {
            while (currentSize >= maxLen){
                condition.await();
            }
            if(!flush){
                queue.offer(msg);
                currentSize++;
            }else {
                flush = false;
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        condition.signal();
        lock.unlock();
    }


    public String getMsg(){

        lock.lock();
        String msg = "";
        try {
            while (currentSize <= 0){
                condition.await();
            }
            msg = queue.poll();
            currentSize--;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        condition.signal();
        lock.unlock();
        return msg;
    }


    public void flush(){
        lock.lock();
        queue.clear();
        currentSize = 0;
        flush = true;
        condition.signal();
        lock.unlock();
    }


}
