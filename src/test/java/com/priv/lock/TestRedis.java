package com.priv.lock;

import com.priv.lock.util.RedisUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by Chen on 7/25/20.
 */
@SpringBootTest
public class TestRedis {
    @Autowired
    RedisUtil redisUtil;

    @Test
    public void testRedis() {
        redisUtil.setKey("123", 23);
        String value = redisUtil.getKey("123");
        assertEquals("23", value);
    }

    @Test
    public void testRedisLua() {
        redisUtil.getLock("a", "b", "123", 2);
        assertTrue(redisUtil.releaseLock("a", "b", "123"));
        String ab = (String) redisUtil.getInstance().opsForValue().get("ab");
        assertNull(ab);
    }

    @Test
    public void testRedisLuaExpendTimeout() throws InterruptedException {
        redisUtil.getLock("a", "b", "123", 2);
        for (int i = 0; i < 3; i++) {
            Thread.sleep(300);
            redisUtil.expendTimeout("a", "b", "123", 2);
        }
        String ab = (String) redisUtil.getInstance().opsForValue().get("ab");
        assertEquals("123", ab);
    }
}
