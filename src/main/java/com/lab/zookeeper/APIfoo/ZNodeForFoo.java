package com.lab.zookeeper.APIfoo;

import org.apache.zookeeper.*;

import java.util.Collection;
import java.util.concurrent.CountDownLatch;

/**
 * Created by zhzy on 2017/6/8.
 */
public class ZNodeForFoo  implements Watcher{

    private static CountDownLatch connectedSemaphore = new CountDownLatch(1);

    public static void main(String[] args) throws Exception{
        String path = "/zk-book-auth_test";
        ZooKeeper zooKeeper = new ZooKeeper("10.108.219.44:2181", 50000, new ZNodeForFoo());
        connectedSemaphore.await();

        //添加带权限信息的节点
        zooKeeper.addAuthInfo("digest", "foo:true".getBytes());
        zooKeeper.create(path, "init".getBytes(), ZooDefs.Ids.CREATOR_ALL_ACL, CreateMode.EPHEMERAL);
    }

    public void process(WatchedEvent watchedEvent) {
        if(Event.KeeperState.SyncConnected == watchedEvent.getState()){
            connectedSemaphore.countDown();
        }
    }
}
