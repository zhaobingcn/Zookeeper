package com.lab.zookeeper;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.util.concurrent.CountDownLatch;

/**
 * Created by zhzy on 2017/6/8.
 */
public class DeleteAPISyncUsage implements Watcher {

    private static CountDownLatch connectedSemaphore  = new CountDownLatch(1);

    public static void main(String[] args) throws Exception{
        ZooKeeper zk = new ZooKeeper("10.108.219.44:2181", 5000, new DeleteAPISyncUsage());
        connectedSemaphore.await();

        /**
         * 删除节点，需要注意的是只允许删除叶子节点
         */
        zk.delete("/persistent-node", -1);
    }

    public void process(WatchedEvent watchedEvent) {
        if(Event.KeeperState.SyncConnected == watchedEvent.getState()){
            if(Event.EventType.None == watchedEvent.getType() && null == watchedEvent.getPath()){
                connectedSemaphore.countDown();
            }
        }
    }


}
