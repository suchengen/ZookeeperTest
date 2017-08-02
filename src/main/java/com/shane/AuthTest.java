package com.shane;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;

/**
 * Created by Shane-PC on 2017/8/1.
 */
public class AuthTest {


    final static String PATH = "/zk-book-auth_test";

    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {


        ConnectionWatcher connectionWatcher = new ConnectionWatcher();
        connectionWatcher.connect(App.HOST);
        ZooKeeper zooKeeper = connectionWatcher.zk;
        zooKeeper.addAuthInfo("digest", "foo:true".getBytes());
        zooKeeper.create(PATH, "init".getBytes(), ZooDefs.Ids.CREATOR_ALL_ACL, CreateMode.EPHEMERAL);

        ConnectionWatcher connectionWatcher1 = new ConnectionWatcher();
        connectionWatcher1.connect(App.HOST);
        ZooKeeper zooKeeper1 = connectionWatcher1.zk;
        zooKeeper1.getData(PATH, false, null);

        connectionWatcher.close();
        connectionWatcher1.close();
    }

}
