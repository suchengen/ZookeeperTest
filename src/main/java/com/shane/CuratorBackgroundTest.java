package com.shane;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CreateBuilder;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Shane-PC on 2017/8/1.
 */
public class CuratorBackgroundTest {

    public final static String PATH = "/curator";

    static CountDownLatch countDownLatch = new CountDownLatch(2);

    public static void main(String[] args) throws Exception {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(App.HOST)
                .retryPolicy(retryPolicy)
                .namespace("base")
                .build();
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        client.start();
        CreateBuilder createBuilder = client.create();
        createBuilder.creatingParentsIfNeeded()
                .withMode(CreateMode.EPHEMERAL)
                .inBackground(new BackgroundCallback() {
            public void processResult(CuratorFramework curatorFramework, CuratorEvent curatorEvent) throws Exception {
                System.out.println("Code: " + curatorEvent.getResultCode() + "  Type: " + curatorEvent.getType());
                System.out.println(Thread.currentThread().getName());
                countDownLatch.countDown();
            }
        }, executorService).forPath(PATH, "init".getBytes());

        createBuilder.creatingParentsIfNeeded()
                .withMode(CreateMode.EPHEMERAL)
                .inBackground(new BackgroundCallback() {
                    public void processResult(CuratorFramework curatorFramework, CuratorEvent curatorEvent) throws Exception {
                        System.out.println("Code: " + curatorEvent.getResultCode() + "  Type: " + curatorEvent.getType());
                        System.out.println(Thread.currentThread().getName());
                        countDownLatch.countDown();
                    }
                }).forPath(PATH, "init".getBytes());
        countDownLatch.await();
        executorService.shutdown();
        client.close();
    }

}
