package com.lab.zookeeper;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * Created by zhzy on 2017/6/8.
 */
//Java API->创建连接->创建一个最基本的Zookeeper对象实例，复用session和password
public class ZooKeeperConstructorUsageWithSidPassword implements Watcher {

    public static CountDownLatch connectedSemphore = new CountDownLatch(1);

    public static void main(String[] args) throws Exception{
        ZooKeeper zookeeper = new ZooKeeper("10.108.219.44:2181", 5000, new
                ZooKeeperConstructorUsageWithSidPassword());
        connectedSemphore.await();

        /**
         * 获取sessionId,password是为了复用会话
         */

        long sessionId = zookeeper.getSessionId();
        byte[] password = zookeeper.getSessionPasswd();

        //使用错误的sessionId和password连接
        zookeeperConnector wrong = new zookeeperConnector(1, "test".getBytes(), new CountDownLatch(1));
        wrong.connect();


        //使用正确的sessionId和password连接
        zookeeperConnector correct = new zookeeperConnector(sessionId, password, new CountDownLatch(1));
        correct.connect();

    }
    public void process(WatchedEvent watchedEvent) {

       System.out.println("Receive watched event:" + watchedEvent);
       if(Event.KeeperState.SyncConnected == watchedEvent.getState()){
           connectedSemphore.countDown();
       }


    }

    static class zookeeperConnector implements Watcher{

        private long sessionId;
        private byte[] password;
        private  CountDownLatch connectedSemphore;

        public zookeeperConnector(long sessionId, byte[] password, CountDownLatch connectedSemphore){
            this.sessionId = sessionId;
            this.password = password;
            this.connectedSemphore = connectedSemphore;
        }

        public void connect() throws IOException, InterruptedException{
            new ZooKeeper("10.108.219.44:2181", 5000,this, sessionId, password);
            this.connectedSemphore.await();
        }

        public void process(WatchedEvent watchedEvent) {
            System.out.println("Receive watched event:" + watchedEvent);
            this.connectedSemphore.countDown();
        }
    }
}
