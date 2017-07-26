package com.shane;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.io.IOException;

/**
 * Created by Shane-PC on 2017/7/26.
 */
public class ConfigWatcher implements Watcher {

    private ActiveKeyValueStore store;

    public void process(WatchedEvent event) {
        if (Event.EventType.NodeDataChanged == event.getType()) {
            try {
                displayConfig();
            } catch (InterruptedException e) {
                System.err.println("Interrupted. exiting. ");
                Thread.currentThread().interrupt();
            } catch(KeeperException e){
                System.out.printf("KeeperException锛?s. Exiting.\n", e);
            }
        }
    }

    public ConfigWatcher(String hosts) throws IOException, InterruptedException {
        store = new ActiveKeyValueStore();
        store.connect(hosts);
    }

    public void displayConfig() throws KeeperException, InterruptedException {
        String value = store.read(ConfigUpdater.PATH, this);
        System.out.printf("Read %s as %s\n",ConfigUpdater.PATH,value);
    }

    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        String hosts = "192.168.3.193:2181";
        ConfigWatcher configWatcher = new ConfigWatcher(hosts);
        configWatcher.displayConfig();
        Thread.sleep(Long.MAX_VALUE);
    }
}
