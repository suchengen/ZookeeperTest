package com.shane;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.*;
import org.apache.curator.framework.recipes.atomic.AtomicValue;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicInteger;
import org.apache.curator.framework.recipes.barriers.DistributedBarrier;
import org.apache.curator.framework.recipes.barriers.DistributedDoubleBarrier;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Shane-PC on 2017/8/1.
 */
public class CuratorTest {

    public final static String PATH = "/curator";

    public static void main(String[] args) throws Exception {
        CyclicBarrierDoubleTest();
    }

    public void Test() throws Exception {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(100, 3);
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(App.HOST)
//                .sessionTimeoutMs(5000)
                .retryPolicy(retryPolicy)
                .namespace("base")
                .build();

//        CuratorFramework client = CuratorFrameworkFactory.newClient(App.HOST, retryPolicy);
        client.start();
//        client.usingNamespace("base");

        CreateBuilder createBuilder = client.create();
        String result = createBuilder
                .creatingParentsIfNeeded()
                .withMode(CreateMode.EPHEMERAL)
                .forPath(PATH, "init".getBytes());
        System.out.println(result);

        Stat stat = new Stat();
        byte[] data = client.getData().storingStatIn(stat).forPath(PATH);
        System.out.println(stat.getMtime());

        SetDataBuilder setDataBuilder = client.setData();
        setDataBuilder.forPath(PATH, "aaff".getBytes());

        DeleteBuilder deleteBuilder = client.delete();
        deleteBuilder.guaranteed()
                .forPath(PATH);

        client.close();
    }

    public static void NoLockTest() {
        final CountDownLatch downLatch = new CountDownLatch(1);

        final String lockPath = "/curator_recipes_lock_path";
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(App.HOST)
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .build();
        client.start();

        final InterProcessMutex lock = new InterProcessMutex(client, lockPath);

        for (int i = 0; i < 100; ++i) {
            new Thread(new Runnable() {
                public void run() {
                    try {
                        downLatch.await();
                        lock.acquire();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    SimpleDateFormat sdf = new SimpleDateFormat(
                            "HH:mm:ss|SSS");
                    String orderNo = sdf.format(new Date());
                    System.err.println("订单号：" + orderNo);
                    try {
                        lock.release();
                    } catch (Exception e) {

                    }
                }
            }).start();
        }
        downLatch.countDown();
    }

    //分布式计数器
    public static void DistAtomicIntTest() throws Exception {
        final String distAtomicIntPath = "/curator_recipes_distatomicint_path";
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(App.HOST)
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .build();
        client.start();

        DistributedAtomicInteger atomicInteger = new DistributedAtomicInteger(client,
                distAtomicIntPath, new RetryNTimes(3, 1000));
        //AtomicValue<Integer> rc = atomicInteger.add(8);
        //System.out.println("Result: " + rc.succeeded());
        AtomicValue<Integer> integerAtomicValue = atomicInteger.get();

        System.out.println(integerAtomicValue.preValue());
    }

    public static DistributedBarrier barrier;
    // 分布式Barrier
    public static void CyclicBarrierTest() throws Exception {
        final String barrierPath = "/curator_recipes_barrier_path";

        final CountDownLatch countDownLatch = new CountDownLatch(5);

        for (int i = 0; i < 5; ++i) {
            new Thread(new Runnable() {
                public void run() {
                    try {
                        CuratorFramework client = CuratorFrameworkFactory.builder()
                                .connectString(App.HOST)
                                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                                .build();
                        client.start();

                        barrier = new DistributedBarrier(client, barrierPath);

                        countDownLatch.countDown();
                        System.out.println(countDownLatch.getCount());

                        System.out.println(Thread.currentThread().getName() + "设置barrier");
                        barrier.setBarrier();
                        barrier.waitOnBarrier();
                        System.err.println("启动。。");


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
        countDownLatch.await();
        Thread.sleep(2000);
        barrier.removeBarrier();
    }

    //分布式BarrierDouble
    public static void CyclicBarrierDoubleTest() throws Exception {

        final int count = 5;
        final String path = "/curator_recipes_double_barrier_path";

        for (int i = 0; i < count; ++i) {
            new Thread(new Runnable() {
                public void run() {

                    CuratorFramework client = CuratorFrameworkFactory.builder()
                            .connectString(App.HOST)
                            .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                            .build();
                    client.start();

                    DistributedDoubleBarrier barrier = new DistributedDoubleBarrier(client, path, count);

                    System.out.println(Thread.currentThread().getName() + "准备");
                    try {
                        barrier.enter();
                        System.out.println(Thread.currentThread().getName() + "跑！");
                        barrier.leave();
                        System.out.println(Thread.currentThread().getName() + "结束");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                }
            }).start();
        }

    }



}
