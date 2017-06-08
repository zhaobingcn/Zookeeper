package com.lab.zookeeper;

import org.apache.zookeeper.*;

import java.util.concurrent.CountDownLatch;

/**
 * Created by zhzy on 2017/6/8.
 */
public class ZooKeeperCreateAPISyncUsage implements Watcher{

    private static CountDownLatch connectedSemaphore = new CountDownLatch(1);

    public static void main(String[] args) throws Exception{
        ZooKeeper zooKeeper = new ZooKeeper("10.108.219.44:2181", 5000, new ZooKeeperCreateAPISyncUsage());
        connectedSemaphore.await();

        /**
         * 创建临时节点
         */
        String path1 = zooKeeper.create("/zk-test-ephemeral-", "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        System.out.println("Success create znode:" + path1);

        /**
         * 创建临时顺序节点
         * Zookeeper会自动在节点后后缀上添加一个数字
         */
        String path2 = zooKeeper.create("/zk-test-ephemeral-", "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        System.out.println("Succession create znode:" + path2);

        /**
         * 创建永久节点
         */
        String path3 = zooKeeper.create("/persistent-node", "zhaobing".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        System.out.println("Success create znode:" +path3);

    }

    public void process(WatchedEvent watchedEvent) {

        if(Event.KeeperState.SyncConnected == watchedEvent.getState()){
            connectedSemaphore.countDown();
        }

    }
}
