package com.shane;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Shane-PC on 2017/7/28.
 */
public class ConcurrentTest {
    private CountDownLatch startSignal = new CountDownLatch(1);
    private CountDownLatch doneSignal = null;
    private CopyOnWriteArrayList<Long> list = new CopyOnWriteArrayList<Long>();
    private AtomicInteger err = new AtomicInteger();
    private ConcurrentTask[] task = null;

    public ConcurrentTest(ConcurrentTask... task) {
        this.task = task;
        if (null == task) {
            System.out.println("task can not null");
            System.exit(1);
        }
        doneSignal = new CountDownLatch(task.length);
        start();
    }

    private void start() {
        createdThread();
        startSignal.countDown();
        try {
            doneSignal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        getExeTime();
    }

    private void createdThread() {
        long length = doneSignal.getCount();
        for (int i = 0; i < length; ++i) {
            final int j = i;
            new Thread(new Runnable() {
                public void run() {
                    try {
                        startSignal.await();
                        long start = System.currentTimeMillis();
                        task[j].run();
                        long end = System.currentTimeMillis() - start;
                        list.add(end);
                    } catch (Exception e) {
                        err.getAndIncrement();
                    }
                    doneSignal.countDown();
                }
            }).start();
        }
    }

    private void getExeTime() {
        int size = list.size();
        List<Long> _list = new ArrayList<Long>(size);
        _list.addAll(list);
        Collections.sort(_list);
        long min = _list.get(0);
        long max = _list.get(size - 1);
        long sun = 0L;
        for (Long t : _list) {
            sun += t;
        }
        long avg = sun / size;
        System.out.println("min: " + min);
        System.out.println("max: " + max);
        System.out.println("avg: " + avg);
        System.out.println("err: " + err.get());
    }

    public interface ConcurrentTask {
        void run();
    }
}
