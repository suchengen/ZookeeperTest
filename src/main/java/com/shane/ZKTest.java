package com.shane;

/**
 * Created by Shane-PC on 2017/7/28.
 */
public class ZKTest {

    public static void main(String[] args) {
//        Runnable task1 = new Runnable() {
//            public void run() {
//                DistributedLock lock = null;
//                try {
//                    lock = new DistributedLock("192.168.3.193:2181", "test1");
//                    lock.lock();
//                    Thread.sleep(3000);
//                    System.out.println("===Thread " + Thread.currentThread().getId() + " running");
//                } catch (Exception e) {
//                    e.printStackTrace();
//                } finally {
//                    if (null != lock) {
//                        lock.unlock();
//                    }
//                }
//            }
//        };
//        new Thread(task1).start();
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        ConcurrentTest.ConcurrentTask[] tasks = new ConcurrentTest.ConcurrentTask[60];
        for (int i = 0; i < tasks.length; ++i) {
            ConcurrentTest.ConcurrentTask task3 = new ConcurrentTest.ConcurrentTask() {
                public void run() {
                    DistributedLock lock = null;
                    try {
                        lock = new DistributedLock("192.168.3.193:2181", "test2");
                        lock.lock();
                        System.out.println("Thread " + Thread.currentThread().getId() + " running");
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        lock.unlock();
                    }
                }
            };
            tasks[i] = task3;
        };
        new ConcurrentTest(tasks);
    }

}
