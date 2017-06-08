package com.lab.zookeeper;

import org.apache.zookeeper.*;

import java.util.concurrent.CountDownLatch;

/**
 * Created by zhzy on 2017/6/8.
 */
public class ExistAPISyncUsage implements Watcher{
    private static CountDownLatch connectedSemaphore = new CountDownLatch(1);
    private static CountDownLatch lastSemaphore = new CountDownLatch(1);
    private static ZooKeeper zk;
    private static String path = "/zk-book";

    public static void main(String[] args) throws Exception{
        zk = new ZooKeeper("10.108.219.44:2181", 5000, new ExistAPISyncUsage());
        connectedSemaphore.await();
        /**
         * 通过exists接口检测是否存在指定节点，同时注册一个Watcher
         */
        zk.exists(path, true);

        /**
         * 创建节点/zk-book，服务器回想客户端发送时间通知:NodeCreated
         * 客户端接到通知以后，继续调用exists接口，注册Watcher
         */
        zk.create(path, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

        /**
         * 更新节点数据，服务器互相客户端发送时间通知：NodeDataChanged
         * 客户端接收到通知之后，唏嘘调用exists接口，注册Watcher
         */
        zk.setData(path, "123".getBytes(), -1);

        /**
         * 删除节点/zk-book
         * 客户端会受到服务端的时间通知：Nodedeleted
         */
        zk.delete(path, -1);

        lastSemaphore.await();
    }

    public void process(WatchedEvent watchedEvent) {
        try {

            if (Event.KeeperState.SyncConnected == watchedEvent.getState()) {
                if (Event.EventType.None == watchedEvent.getType() && null == watchedEvent.getPath()) {
                    connectedSemaphore.countDown();
                } else if (Event.EventType.NodeCreated == watchedEvent.getType()) {
                    System.out.println("Node(" + watchedEvent.getPath() + ")Created");
                    zk.exists(watchedEvent.getPath(), true);
                } else if (Event.EventType.NodeDeleted == watchedEvent.getType()) {
                    System.out.println("Node(" + watchedEvent.getPath() + ")Deleted");
                    zk.exists(watchedEvent.getPath(), true);
                    System.out.println("last semaphore");
                    lastSemaphore.countDown();
                } else if (Event.EventType.NodeDataChanged == watchedEvent.getType()) {
                    System.out.println("Node(" + watchedEvent.getPath() + ")DataChanged");
                    zk.exists(watchedEvent.getPath(), true);
                }

            }
        }catch (Exception e){

        }
    }
}
