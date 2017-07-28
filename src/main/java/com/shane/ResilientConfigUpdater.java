package com.shane;

import org.apache.zookeeper.KeeperException;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by Shane-PC on 2017/7/27.
 */
public class ResilientConfigUpdater extends ConnectionWatcher {

    private static final String PATH = "/config";
    private ChangedActiveKeyValueStore store;
    private Random random = new Random();

    public ResilientConfigUpdater(String hosts) throws IOException, InterruptedException {
        store = new ChangedActiveKeyValueStore();
        store.connect(hosts);


    }

    public void run() throws InterruptedException, KeeperException {
        while (true) {
            String value = random.nextInt(100) + "";
            store.write(PATH, value);
            System.out.printf("Set %s to %s\n", PATH, value);
            TimeUnit.SECONDS.sleep(random.nextInt(10));
        }
    }

    public static void main(String[] args) throws Exception {
        while (true) {
            try {
                ResilientConfigUpdater configUpdater = new ResilientConfigUpdater("192.168.3.193:2181");
                configUpdater.run();
            } catch (KeeperException.SessionExpiredException e) {

            } catch (KeeperException e) {
                e.printStackTrace();
                break;
            }
        }
    }

}
