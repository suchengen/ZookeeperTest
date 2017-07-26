package com.shane;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;

/**
 * Created by Shane-PC on 2017/7/25.
 */
public class JoinGroup extends ConnectionWatcher {

    public void join(String groupName, String memberName)
            throws KeeperException, InterruptedException {
        String path = "/" + groupName + "/" + memberName;
        String createdPath = zk.create(path, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        System.out.println("Created: " + createdPath);
    }



}
