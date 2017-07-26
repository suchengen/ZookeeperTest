package com.shane;

import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Shane-PC on 2017/7/25.
 */
public class CreateGroup implements Watcher {

    private static final int SESSION_TIMEOUT = 5000;

    private ZooKeeper zk;

    private CountDownLatch connectedSignal = new CountDownLatch(1);

    public void process(WatchedEvent event) {
        if (event.getState() == Event.KeeperState.SyncConnected) {
            connectedSignal.countDown();
        }
    }

    private void close() throws InterruptedException {
        zk.close();
    }

    private void create(String groupName) throws KeeperException, InterruptedException {
        String path = "/" + groupName;
        if (null == zk.exists(path, false)) {
            zk.create(path,  "data".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            System.out.println("created: " + path);
        }
    }

    private void connect(String hosts) throws IOException, InterruptedException {
        zk = new ZooKeeper(hosts, SESSION_TIMEOUT, this);
        connectedSignal.await();
    }

    public static void main(String[] args) throws IOException,
            InterruptedException, KeeperException {
        CreateGroup createGroup = new CreateGroup();

        createGroup.connect("192.168.3.193:2181");
        createGroup.create("zk1");
        createGroup.close();

    }

}
