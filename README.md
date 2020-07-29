# Distributed Lock

Although there are many excellent implementations of distributed locks exist, like Redisson, it does not make our project redundant.
On the contrary, implementing something that has already been implemented can help us understand it better. 

This is a Distributed lock based on Redis.

### Features

- Easy to use. You can lock a function by `LockAnnotation`, and use its parameters as the lock key. It support the SpEl expression.
    ```java
        @LockAnnotation(lockField = "test", lockKey = "'key='+#key", waitTime = -1)
        public void testFunc(String key){....}
    ```
    Or you can use it in the method, the code will be like `LockAspect.class`.
- Using Lua script for executing batch of redis operations atomically.
- Using Daemon Thread to reset the expire time of lock key periodically in redis to make sure there will be only one thread hold the lock at same time.
- Using the logic of Java ReentrantLock to make this distributed lock reenterable.

### Future optimizations
- Can use the pub/sub of redis to let the thread try less time when it is waiting for the lock, which can largely enhance the performance.