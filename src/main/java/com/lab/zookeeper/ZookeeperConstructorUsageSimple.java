package com.lab.zookeeper;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.util.concurrent.CountDownLatch;

/**
 * Created by zhzy on 2017/6/8.
 */
//java api -》 创建连接 ->创建一个最基本的zookeeper对象实例
public class ZookeeperConstructorUsageSimple implements Watcher {

    private static CountDownLatch connectedSemaphore = new CountDownLatch(1);

    public static void main(String[] args) throws Exception{
        /**
         * Zookeeper客户端和服务端的会话的建立是一个异步的过程
         * 也就是说在程序中，构造方法会在处理完一个客户端初始化工作之后立即返回
         * 在大多数的情况下此时并没有真正建立好一个可用的会话，此时在会话的生命周期中处于"CONNECTING"
         */
        ZooKeeper zooKeeper = new ZooKeeper("10.108.219.44", 5000, new ZookeeperConstructorUsageSimple());
        System.out.println(zooKeeper.getState());

        try {
            //等待watcher通知SyncConnected
            connectedSemaphore.await();
        }catch (InterruptedException e){
        }
        System.out.println("zookeeper session esablished");
    }

    /**
     * 该类实现了Watcher接口，重写了process方法
     * 该方法负责处理来之Zookeeper服务端的Watcher通知，即服务端建立连接后会调用该方法
     *
     * @param watchedEvent
     */
    public void process(WatchedEvent watchedEvent) {

        System.out.println("Receive watched event:" + watchedEvent);
        if(Event.KeeperState.SyncConnected == watchedEvent.getState()){
            connectedSemaphore.countDown();
        }
    }
}
