package com.lab.zookeeper;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.concurrent.CountDownLatch;

/**
 * Created by zhzy on 2017/6/8.
 */
public class SetDataAPISuncUsage implements Watcher {

    private static CountDownLatch connectedSemaphore = new CountDownLatch(1);

    public static void main(String[] args) throws Exception{
        String path = "/zk-book";
        ZooKeeper zk = new ZooKeeper("10.108.219.44:2181", 5000, new SetDataAPISuncUsage());
        connectedSemaphore.await();

        zk.create(path, "123".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);

        /**
         * version:-1,代表不需要根据版本号更新
         */

        Stat stat = zk.setData(path, "456".getBytes(), -1);
        System.out.println(stat.getCzxid() + "," +
                            stat.getMzxid() + "," +
                stat.getVersion()
        );

        /**
         * 根据上一次更新的版本号，成功
         */

        Stat stat2 = zk.setData(path, "456".getBytes(), stat.getVersion());
        System.out.println(stat.getCzxid() + "," +
                            stat.getMzxid() + "," +
                            stat.getVersion()
        );

        /**
         * 根据上一次的版本号更新，失败抛异常
         */

        try{
            zk.setData(path, "456".getBytes(), stat.getVersion());
        }catch (KeeperException e){
            System.out.println("Error:" + e.code() + "," +e.getMessage());
        }



    }
    public void process(WatchedEvent watchedEvent) {
        if(Event.KeeperState.SyncConnected == watchedEvent.getState()){
            if(Event.EventType.None == watchedEvent.getType() && null == watchedEvent.getPath()){
                connectedSemaphore.countDown();
            }
        }
    }
}
