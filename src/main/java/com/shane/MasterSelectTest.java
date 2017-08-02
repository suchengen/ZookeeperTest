package com.shane;

import com.sun.istack.internal.NotNull;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * Created by Shane-PC on 2017/8/2.
 */
public class MasterSelectTest {

    public final static String MASTER_PATH = "/curator_recipes_master_path";

    public static void main(String[] args) throws InterruptedException {
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(App.HOST)
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .build();
        client.start();
        LeaderSelector selector = new LeaderSelector(client, MASTER_PATH, new LeaderSelectorListenerAdapter() {
            public void takeLeadership(CuratorFramework curatorFramework) throws Exception {
                System.out.println("成为Master角色");
                Thread.sleep(3000);
                System.out.println("完成Master操作，释放Master权利");
            }
        });
        selector.autoRequeue();
        selector.start();

        Thread.sleep(Integer.MAX_VALUE);
    }

}
