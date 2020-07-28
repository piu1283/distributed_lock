package com.priv.lock;

import com.priv.lock.distributed_lock.redis.service.AddingService;
import com.priv.lock.distributed_lock.redis.worker.AddingWorker;
import com.priv.lock.util.RedisUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chen on 7/25/20.
 */
@SpringBootTest
public class TestRedisLock {
    @Autowired
    AddingService addingService;

    @Autowired
    RedisUtil redisUtil;

    @Test
    public void testRedisLockAdding() throws InterruptedException {
        String key = "ttt";
        redisUtil.getInstance().delete(key);
        int each = 10000 / 5;
        List<Thread> tList = new ArrayList<>();
        for(int i = 0; i < 5; i++){
            Thread t = new Thread(new AddingWorker(each, 0, key, addingService),i+"");
            t.start();
            tList.add(t);
        }
        for (Thread t : tList) {
            t.join();
        }
        String res = redisUtil.getKey(key);
        Assertions.assertEquals("10000", res);
    }

    @Test
    public void testRedisLockAddingWithTimeoutExpend() throws InterruptedException {
        String key = "ttt";
        redisUtil.getInstance().delete(key);
        int each = 100 / 5;
        List<Thread> tList = new ArrayList<>();
        for(int i = 0; i < 5; i++){
            // the executing duration is a little bit larger than lock time
            Thread t = new Thread(new AddingWorker(each, 3, key, addingService),i+"");
            t.start();
            tList.add(t);
        }
        for (Thread t : tList) {
            t.join();
        }
        String res = redisUtil.getKey(key);
        Assertions.assertEquals("100", res);
    }
}
