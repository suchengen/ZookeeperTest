package com.shane;

import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;

import java.util.List;

/**
 * Created by Shane-PC on 2017/7/28.
 */
public class Locks extends TestMainClient {

    String myZnode;

    public Locks(String hosts, String root) {
        super(hosts);
        this.root = root;
        if (null != zooKeeper) {
            try {
                if (null == (zooKeeper.exists(root, false))) {
                    zooKeeper.create(root, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                }
            }catch (KeeperException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
