package com.priv.lock.distributed_lock.redis;

import com.priv.lock.distributed_lock.redis.manager.LockManager;
import com.priv.lock.util.RedisUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class LockAspect {

    private static final Logger logger = LoggerFactory.getLogger(LockAspect.class);

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    LockManager manager;

    @Around("@annotation(com.priv.lock.distributed_lock.redis.LockAnnotation)")
    public Object aroundLock(ProceedingJoinPoint joinPoint) {
        ExpressionParser parser = new SpelExpressionParser();
        LocalVariableTableParameterNameDiscoverer discoverer = new LocalVariableTableParameterNameDiscoverer();
        EvaluationContext context = new StandardEvaluationContext();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        LockAnnotation la = signature.getMethod().getAnnotation(LockAnnotation.class);
        Object[] args = joinPoint.getArgs();
        String[] params =
                discoverer.getParameterNames(signature.getMethod());
        if (params != null && params.length > 0) {
            for (int i = 0; i < params.length; i++) {
                context.setVariable(params[i], args[i]);
            }
        }
        Expression exp = parser.parseExpression(la.lockKey());
        String key = exp.getValue(context, String.class);
        Lock lock = manager.getLock(la.lockField(), key);
        return lock.execute(() -> {
            try {
                return joinPoint.proceed(args);
            } catch (Throwable throwable) {
                logger.error("Error happen when execute locked function.", throwable);
            }
            return null;
        }, la.waitTime());
    }
}
