package com.priv.lock.distributed_lock.redis.service;

import com.priv.lock.distributed_lock.redis.LockAnnotation;
import com.priv.lock.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class AddingService {

    @Autowired
    RedisUtil redisUtil;

    @LockAnnotation(lockField = "test", lockKey = "'key='+#key", lockTime = 2, waitTime = -1)
    public void addOne(String key, int executeDuration) {
        String v = redisUtil.getKey(key);
        if (executeDuration > 0) {
            try {
                Thread.sleep(executeDuration * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (v == null) {
            redisUtil.setKey(key, 1);
        } else {
            redisUtil.setKey(key,Integer.parseInt(v) + 1);
        }
    }
}
