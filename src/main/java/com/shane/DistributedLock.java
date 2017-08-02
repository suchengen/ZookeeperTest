package com.shane;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * Created by Shane-PC on 2017/7/28.
 */
public class DistributedLock implements Lock, Watcher {

    private ZooKeeper zooKeeper;
    private String root = "/locks";
    private String lockName;
    private String waitNode;
    private String myZnode;
    private CountDownLatch countDownLatch;
    private int sessionTimeout = 30000;
    private List<Exception> exceptionList = new ArrayList<Exception>();

    public DistributedLock(String config, String lockName) {
        this.lockName = lockName;
        try {
            zooKeeper = new ZooKeeper(config, sessionTimeout, this);

            Stat stat = zooKeeper.exists(root, false);
            if (null == stat) {
                zooKeeper.create(root, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (IOException e) {
            e.printStackTrace();
            exceptionList.add(e);
        } catch (InterruptedException e) {
            e.printStackTrace();
            exceptionList.add(e);
        } catch (KeeperException e) {
            e.printStackTrace();
            exceptionList.add(e);
        }
    }

    public void lock() {
        if (0 < exceptionList.size()) throw new LockException(exceptionList.get(0));
        try {
            if (tryLock()) {
                System.out.println("Thread " + Thread.currentThread().getId()
                        + " " + myZnode + " get lock true");
                return;
            } else {
                waitForLock(waitNode, sessionTimeout);
            }
        } catch (KeeperException e) {
            throw new LockException(e);
        } catch (InterruptedException e) {
            throw new LockException(e);
        }
    }

    public void lockInterruptibly() throws InterruptedException {
        this.lock();
    }

    public boolean tryLock() {
        try {
            String splitStr = "_lock_";
            if (lockName.contains(splitStr)) throw new LockException("lockName can not contains \\u000B");
            myZnode = zooKeeper.create(root + "/" + lockName + splitStr, new byte[0],
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            System.out.println(myZnode + " is created");
            List<String> subNodes = zooKeeper.getChildren(root, false);
            List<String> lockObjNodes = new ArrayList<String>();
            for (String node : subNodes) {
                String _node = node.split(splitStr)[0];
                if (_node.equals(lockName)) {
                    lockObjNodes.add(node);
                }
            }
            Collections.sort(lockObjNodes);
            System.out.println(myZnode + "==" + lockObjNodes.get(0));
            if (myZnode.equals(root + "/" + lockObjNodes.get(0))) {
                return true;
            }
            String subMyZnode = myZnode.substring(myZnode.lastIndexOf("/") + 1);
            waitNode = lockObjNodes.get(Collections.binarySearch(lockObjNodes, subMyZnode) - 1);
        } catch (KeeperException e) {
            throw new LockException(e);
        } catch (InterruptedException e) {
            throw new LockException(e);
        }
        return false;
    }

    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        try {
            if (this.tryLock()) {
                return true;
            }
            return waitForLock(waitNode, time);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void unlock() {
        try {
            System.out.println("unlock " + myZnode);
            zooKeeper.delete(myZnode, -1);
            myZnode = null;
            zooKeeper.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

    public Condition newCondition() {
        return null;
    }

    public void process(WatchedEvent event) {
        if (null != countDownLatch) {
            System.out.println("progress: " + event.getType().toString());
            countDownLatch.countDown();
        }
    }

    private boolean waitForLock(String lower, long waitTime)
            throws InterruptedException, KeeperException {
        Stat stat = zooKeeper.exists(root + "/" + lower, true);
        if (null != stat) {
            System.out.println("Thread " + Thread.currentThread().getId()
                    + " waiting for " + root + "/" + lower);
            this.countDownLatch = new CountDownLatch(1);
            this.countDownLatch.await(waitTime, TimeUnit.MILLISECONDS);
            this.countDownLatch = null;
        }
        return true;
    }

    public class LockException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        public LockException(String e) {
            super(e);
        }
        public LockException(Exception e) {
            super(e);
        }
    }
}
