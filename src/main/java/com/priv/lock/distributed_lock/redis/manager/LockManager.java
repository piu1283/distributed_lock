package com.priv.lock.distributed_lock.redis.manager;

import com.priv.lock.distributed_lock.redis.Lock;
import com.priv.lock.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class LockManager {

    @Autowired
    RedisUtil redisUtil;

    public static ConcurrentHashMap<String, Lock> lockMap = new ConcurrentHashMap<>();

    public Lock getLock(String lockField, String lockKey) {
        String lockName = StringUtils.isEmpty(lockField) ? lockKey : redisUtil.getRedisLockKey(lockField, lockKey);
        if (lockMap.containsKey(lockName)) {
            return lockMap.get(lockName);
        } else {
            synchronized (this) {
                if (!lockMap.containsKey(lockName)) {
                    Lock lock = new Lock(redisUtil, lockField, lockKey);
                    lockMap.putIfAbsent(lockName, lock);
                }
            }
            return lockMap.get(lockName);
        }
    }
}
