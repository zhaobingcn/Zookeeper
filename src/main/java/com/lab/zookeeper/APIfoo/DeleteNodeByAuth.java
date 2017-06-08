package com.lab.zookeeper.APIfoo;

import com.lab.zookeeper.DeleteAPISyncUsage;
import org.apache.zookeeper.*;

import java.util.concurrent.CountDownLatch;

/**
 * Created by zhzy on 2017/6/8.
 */
public class DeleteNodeByAuth implements Watcher {

    private static CountDownLatch createSemaphore = new CountDownLatch(1);
    private static CountDownLatch deleteChildNoAuthSemaphore = new CountDownLatch(1);
    private static CountDownLatch deleteChildAuthSemaphore = new CountDownLatch(1);
    private static CountDownLatch deleteNoAuthSemaphore = new CountDownLatch(1);

    public static void main(String[] args) throws Exception{
        String path = "/zk-book-auth_test";
        String pathChild = "/zk-book-auth_test/child";

        ZooKeeper createZK = new ZooKeeper("10.108.219.44:2181", 5000, new DeleteNodeByAuth());
        createSemaphore.await();
        createZK.addAuthInfo("digest", "foo:true".getBytes());
        createZK.create(pathChild, "initChild".getBytes(), ZooDefs.Ids.CREATOR_ALL_ACL, CreateMode.PERSISTENT);

        try{
            ZooKeeper deleteChildNoAuthZK = new ZooKeeper("10.108.219.44:2181", 50000, new DeleteNodeByAuth());
            deleteChildNoAuthSemaphore.await();
            deleteChildNoAuthZK.delete(pathChild, -1);
        }catch (Exception e){
            System.out.println("删除节点失败");
        }

        ZooKeeper deleteChildAuthZK = new ZooKeeper("10.108.219.44:2181", 50000, new DeleteNodeByAuth());
        deleteChildAuthSemaphore.await();
        deleteChildAuthZK.addAuthInfo("digest", "foo:true".getBytes());
        deleteChildAuthZK.delete(pathChild, -1);
        System.out.println("成功删除节点：" + pathChild);

        /**
         * 删除权限作用的范围是子节点，所有不包含权限信息的客户端可以删除/zk-book-auth_test节点
         */

        ZooKeeper deleteNoAuthZK = new ZooKeeper("10.108.219.44:2181", 50000, new DeleteNodeByAuth());
        deleteNoAuthSemaphore.await();
        deleteNoAuthZK.delete(path, -1);
        System.out.print("成功删除节点：" + path);
    }

    public void process(WatchedEvent watchedEvent) {
        if(Event.KeeperState.SyncConnected == watchedEvent.getState()){
            if(createSemaphore.getCount() > 0){
                createSemaphore.countDown();
                return;
            }
            if(deleteChildNoAuthSemaphore.getCount() > 0){
                deleteChildNoAuthSemaphore.countDown();
                return;
            }
            if(deleteChildAuthSemaphore.getCount() > 0){
                deleteChildAuthSemaphore.countDown();
                return;
            }
            if(deleteNoAuthSemaphore.getCount() > 0){
                deleteNoAuthSemaphore.countDown();
                return;
            }
        }
    }
}
