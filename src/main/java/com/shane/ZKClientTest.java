package com.shane;

import org.I0Itec.zkclient.*;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Shane-PC on 2017/8/1.
 */
public class ZKClientTest {

    public final static String PATH = "/zookeeper_book";

    public static void main(String[] args) throws InterruptedException {
        ZkClient zkClient = new ZkClient(App.HOST, 5000);

        IZkChildListener iZkChildListener = new IZkChildListener() {
            public void handleChildChange(String s, List<String> list) throws Exception {
                System.out.println(s + " 's child changed, currentChilds: " + list);
            }
        };

        IZkDataListener iZkDataListener = new IZkDataListener() {
            public void handleDataChange(String s, Object o) throws Exception {
                System.out.println(s + " 's data changed, currentValue: " + o);
            }

            public void handleDataDeleted(String s) throws Exception {
                System.out.println(s + " 's data deleted");
            }
        };

        IZkStateListener iZkStateListener = new IZkStateListener() {
            public void handleStateChanged(Watcher.Event.KeeperState keeperState) throws Exception {
                System.out.println(keeperState);
            }

            public void handleNewSession() throws Exception {

            }

            public void handleSessionEstablishmentError(Throwable throwable) throws Exception {

            }
        };

        zkClient.subscribeChildChanges(PATH, iZkChildListener);
        zkClient.subscribeDataChanges(PATH, iZkDataListener);
        zkClient.subscribeStateChanges(iZkStateListener);

        zkClient.createEphemeral(PATH, "hello zk");
        Thread.sleep(1000);

        Stat stat = new Stat();
        String value = zkClient.readData(PATH, stat);
        Thread.sleep(1000);

        zkClient.writeData(PATH, "hello write data");
        Thread.sleep(1000);

        zkClient.delete(PATH);
        Thread.sleep(1000);

        zkClient.close();
    }

}
