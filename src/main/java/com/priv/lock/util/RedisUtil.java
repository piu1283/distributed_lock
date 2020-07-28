package com.priv.lock.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;

/**
 * Created by Chen on 3/18/20.
 */
@Component
public class RedisUtil {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private final String deleteScript = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

    private final String expendTimeoutScript = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('expire', KEYS[1], ARGV[2]) else return 0 end";

    public RedisTemplate<String, String> getInstance() {
        return redisTemplate;
    }

    /**
     *
     * @param field the prefix
     * @param methodKey method identification
     * @param randNum a random number to prevent release the wrong lock
     * @param lockTime the lock expire time (second)
     * @return success or not
     */
    public boolean getLock(String field, String methodKey,String randNum, int lockTime) {
        String key = getRedisLockKey(field, methodKey);
        Duration lockDuration = Duration.ofSeconds(lockTime);
        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, randNum, lockDuration);
        return success == null ? false : success;
    }

    public boolean releaseLock(String field, String methodKey, String randNum) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setResultType(Long.class);
        script.setScriptText(deleteScript);
        Long execute = redisTemplate.execute(script, Collections.singletonList(getRedisLockKey(field, methodKey)),
                randNum);
        return execute != null && execute > 0;
    }

    public boolean expendTimeout(String field, String key, String value, int lockTime) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(expendTimeoutScript);
        Long res = redisTemplate.execute(script, Collections.singletonList(getRedisLockKey(field, key)), value,
                String.valueOf(lockTime));
        return res != null && res > 0L;
    }

    public String getRedisLockKey(String field, String methodKey) {
        return field + "_" + methodKey;
    }

    public String getKey(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void setKey(String key, int v) {
        redisTemplate.opsForValue().set(key, String.valueOf(v));
    }
}

