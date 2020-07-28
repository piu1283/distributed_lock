package com.priv.lock.distributed_lock.redis;

import com.priv.lock.distributed_lock.redis.worker.TimeoutExpendWorker;
import com.priv.lock.util.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.function.Supplier;

public class Lock {

    private static final Logger LOGGER = LoggerFactory.getLogger(Lock.class);
    private volatile int status;
    private RedisUtil redisUtil;
    private int lockTime = 30;
    private int sleepTime = 100;
    private String lockField;
    private String lockKey;
    private TimeoutExpendWorker TimeoutExpendWorker;
    private Thread TimeoutExpendWorkerThread;
    private Thread lockOwnerThread;
    private String lockName;

    public com.priv.lock.distributed_lock.redis.worker.TimeoutExpendWorker getTimeoutExpendWorker() {
        return TimeoutExpendWorker;
    }

    public Thread getTimeoutExpendWorkerThread() {
        return TimeoutExpendWorkerThread;
    }

    public Thread getLockOwnerThread() {
        return lockOwnerThread;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setLockOwnerThread(Thread lockOwnerThread) {
        this.lockOwnerThread = lockOwnerThread;
    }

    public void setTimeoutExpendWorker(com.priv.lock.distributed_lock.redis.worker.TimeoutExpendWorker timeoutExpendWorker) {
        TimeoutExpendWorker = timeoutExpendWorker;
    }

    public void setTimeoutExpendWorkerThread(Thread timeoutExpendWorkerThread) {
        TimeoutExpendWorkerThread = timeoutExpendWorkerThread;
    }

    public Lock(RedisUtil redisUtil, String lockField, String lockKey) {
        this.status = 0;
        this.redisUtil = redisUtil;
        this.lockField = lockField;
        this.lockKey = lockKey;
        this.lockName = redisUtil.getRedisLockKey(lockField, lockKey);
    }

    final Boolean tryLock(String lockValue, int waitTime) {
        long endTime = System.currentTimeMillis() + waitTime * 1000;
        boolean mustExecute = waitTime < 0;
        try {
            do {
                final Thread current = Thread.currentThread();
                int c = this.getStatus();
                if (c == 0) {
                    if (redisUtil.getLock(lockField, lockKey, lockValue, lockTime)) {
                        lockOwnerThread = current;
                        this.setStatus(c + 1);
                        TimeoutExpendWorker = new TimeoutExpendWorker(lockField, lockKey, lockValue, lockTime,
                                redisUtil);
                        TimeoutExpendWorkerThread = new Thread(TimeoutExpendWorker);
                        TimeoutExpendWorkerThread.setDaemon(true);
                        TimeoutExpendWorkerThread.start();
                        LOGGER.info("Create ReEntrantLock successful, lock name [{}]", lockName);
                        return Boolean.TRUE;
                    }
                } else if (lockOwnerThread == Thread.currentThread()) {
                    if (c + 1 < 0) {
                        throw new Error("Maximum lock count exceeded");
                    }
                    this.setStatus(c + 1);
                    LOGGER.info("entering exist ReEntrantLock successful [{}]. Current LockCount [{}]", lockName, status);
                    return Boolean.TRUE;
                }
                if (waitTime > 0) {
                    LOGGER.info("Current thread failed to obtain lock, wait for [{}ms],lock name [{}]", sleepTime,
                            lockName);
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        LOGGER.info("Interrupted during waiting lock [{}]", lockName, e);
                    }
                }
            } while (mustExecute || System.currentTimeMillis() <= endTime);
            LOGGER.info("Fail to obtain lock, give up waiting for lock : [{}]", lockName);
            return Boolean.FALSE;
        } catch (Exception e) {
            return Boolean.FALSE;
        }
    }

    final void unLock(String lockValue) {
        if (lockOwnerThread == Thread.currentThread()) {
            int c = this.getStatus() - 1;
            if (c == 0) {
                this.setLockOwnerThread(null);
                TimeoutExpendWorker.stopRunning();
                TimeoutExpendWorkerThread.interrupt();
                this.setTimeoutExpendWorker(null);
                this.setTimeoutExpendWorkerThread(null);
                this.setStatus(c);
                // release lock in redis in the last step
                redisUtil.releaseLock(lockField, lockKey, lockValue);
                LOGGER.info("ReEntrantLock has been released successfully,lock name [{}]", lockName);
            } else {
                this.setStatus(c);
                LOGGER.info("ReEntrantLock lockCount-1,lock name [{}]，remain lockCount [{}]", lockName, c);
            }
        }
    }

    public <T> T execute(Supplier<T> supplier, int waitTime) {
        String value = UUID.randomUUID().toString();
        Boolean hasLock = Boolean.FALSE;
        try {
            if (hasLock = this.tryLock(value, waitTime)) {
                return supplier.get();
            }
            return null;
        } catch (Exception e) {
            LOGGER.error("execute error", e);
            return null;
        } finally {
            if (hasLock) {
                this.unLock(value);
            }
        }
    }

}
