package com.lab.zookeeper;

import org.apache.zookeeper.*;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by zhzy on 2017/6/8.
 */
public class ZookeeperGetChildrenAPISunUsage implements Watcher {

    private static CountDownLatch connectedSemaphore = new CountDownLatch(1);
    private static CountDownLatch watcherSemaphore = new CountDownLatch(1);
    private static ZooKeeper zk = null;

    public static void main(String[] args) throws Exception{
        /**
         * 声明node路径
         * 实例化Zookeeper
         */
        String path = "/zk-book";
        zk = new ZooKeeper("10.108.219.44:2181", 5000, new ZookeeperGetChildrenAPISunUsage());
        connectedSemaphore.await();

        /**
         * 创建永久节点/zk-book
         * 创建临时节点/zk-book/c1
         *
         */
        zk.create(path, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        zk.create(path+"/c1", "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);

        /**
         * 获取/zk-book下的子节点
         * 此时注册了默认的watch, 如果在/zk-book下增加节点的话，会调用process方法，通知客户端节点变化了
         * 但是仅仅发出通知，客户端需要自己去再次查询
         * 另外需要注意的是watcher是一次性的，即一旦触发一次通知后，该watcher就失效了
         * 即process方法中的getchildren继续注册了watcher
         */
        List<String> childrenList = zk.getChildren(path, true);
        System.out.println(childrenList);

        zk.create(path + "/c2", "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        watcherSemaphore.await();
    }

    public void process(WatchedEvent watchedEvent) {
        if(Event.KeeperState.SyncConnected == watchedEvent.getState()){
            if(Event.EventType.None == watchedEvent.getType() && null == watchedEvent.getPath()){
                connectedSemaphore.countDown();
            }else if(watchedEvent.getType() == Event.EventType.NodeChildrenChanged){
                try {
                    //收到子节点变动通知，重新主动查询子节点信息
                    System.out.println("Reget Child:" + zk.getChildren(watchedEvent.getPath(), true));
                    watcherSemaphore.countDown();
                }catch (Exception e){

                }
            }
        }
    }
}
