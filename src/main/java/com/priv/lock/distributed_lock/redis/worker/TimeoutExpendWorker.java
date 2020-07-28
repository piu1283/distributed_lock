package com.priv.lock.distributed_lock.redis.worker;

import com.priv.lock.util.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeoutExpendWorker implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(TimeoutExpendWorker.class);

    private RedisUtil redisUtil;
    private String field;
    private String key;
    private String value;
    private int lockTime;

    public TimeoutExpendWorker(String field, String key, String value, int lockTime, RedisUtil ru) {
        this.field = field;
        this.key = key;
        this.value = value;
        this.lockTime = lockTime;
        this.redisUtil = ru;
    }

    private volatile boolean stop = false;

    public void stopRunning() {
        this.stop = true;
    }

    @Override
    public void run() {
        // expend timeout in the 1/2 time point
        long waitTime = lockTime * 1000 / 2;
        logger.info("Timeout Expend thread start, field: [{}], key:[{}], randNum: [{}]",field, key, value);
        while (!stop) {
            try {
                Thread.sleep(waitTime);
                if (redisUtil.expendTimeout(field, key, value, lockTime)) {
                    logger.info("Expend timeout success, field: [{}], key:[{}], randNum: [{}]",field, key, value);
                }else{
                    logger.info("Expend timeout fail, field: [{}], key:[{}], randNum: [{}]",field, key, value);
                    this.stopRunning();
                    break;
                }
            } catch (InterruptedException e) {
                logger.info("Expend timeout worker has been interrupted, field: [{}], key:[{}], randNum: [{}]",field, key, value);
            }

        }
        logger.info("Timeout Expend thread exit, field: [{}], key:[{}], randNum: [{}]",field, key, value);
    }
}
