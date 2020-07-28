//package com.priv.lock.command;
//
//import com.priv.lock.distributed_lock.redis.test.AddingWorker;
//import com.priv.lock.util.RedisUtil;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.shell.standard.ShellComponent;
//import org.springframework.shell.standard.ShellMethod;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * Created by Chen on 7/23/20.
// */
//@ShellComponent
//public class TestCommands {
//    @Autowired
//    RedisUtil redisUtil;
//
//    @ShellMethod("Just add two number together")
//    public int add(int a, int b) {
//        return a + b;
//    }
//    @ShellMethod("test the distributed lock by adding one to a redis key")
//    public Integer TestMultiThreadAdd(int threadNum, int total, long executingTime) throws InterruptedException {
//        String key = "ttt";
//        int each = threadNum / total;
//        List<Thread> tList = new ArrayList<>();
//        for(int i = 0; i < threadNum; i++){
//            Thread t = new Thread(new AddingWorker(each, executingTime, key),i+"");
//            System.out.println("123123123");
//            tList.add(t);
//        }
//        for (Thread t : tList) {
//            t.join();
//        }
//        return redisUtil.getKeyInteger(key);
//    }
//}
