package com.priv.lock.distributed_lock.redis;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface LockAnnotation {
    /**
     * prefix of the key
     *
     * @return
     */
    String lockField() default "";

    /**
     * lock key
     *
     * Support SpEl Expression.
     * You can use plain String, like "abc".
     * Or the parameter in method, like "public void push(String uid){ .... }", the key can be "'uid='+#uid".
     * @return
     */
    String lockKey() default "";

    /**
     * The lock expire time (second)
     * After lockTime, the lock will be automatically released
     *
     * @return
     */
    int lockTime() default 30;

    /**
     * Total lock waiting time (second)
     *
     * @return
     */
    int waitTime() default 10;
}
