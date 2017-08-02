package com.shane;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;

/**
 * Created by Shane-PC on 2017/7/28.
 */
public class TestMainClient implements Watcher {

    protected static ZooKeeper zooKeeper = null;
    protected static Integer mutex;
    int sessionTimeOut = 10000;
    protected String root;

    public TestMainClient(String hosts) {
        if (null == zooKeeper) {
            try {
                System.out.println("创建一个新连接");
                zooKeeper = new ZooKeeper(hosts, sessionTimeOut, this);
                mutex = new Integer(-1);
            } catch (IOException e) {
                zooKeeper = null;
            }
        }
    }

    public synchronized void process(WatchedEvent event) {
        synchronized (mutex) {
            mutex.notify();
        }
    }
}
