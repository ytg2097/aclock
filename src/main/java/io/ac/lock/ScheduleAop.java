package io.ac.lock;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @description:
 * @author: yangtg
 * @create: 2020-02-18
 **/
@Aspect
public class ScheduleAop {
    private static final Logger log = LoggerFactory.getLogger(ScheduleAop.class);
    @Resource
    private ACLock acLock;

    @Pointcut("@annotation(org.springframework.scheduling.annotation.Scheduled)")
    public void annotationPointCut() {
    }

    @Around("annotationPointCut()")
    public Object around(ProceedingJoinPoint proceedingJoinPoint) {
        Class<?> clazz = proceedingJoinPoint.getTarget().getClass();

        String methodName = proceedingJoinPoint.getSignature().getName();


        Method targetMethod = Arrays.stream(clazz.getMethods()).filter(method -> methodName.equals(method.getName())).findFirst().orElse(null);

        if (targetMethod != null && targetMethod.isAnnotationPresent(DistributedScheduled.class)) {
            return this.distributedScheduled(proceedingJoinPoint, targetMethod);
        } else {
            try {
                return proceedingJoinPoint.proceed();
            } catch (Throwable var10) {
                var10.printStackTrace();
                return null;
            }
        }
    }

    private Object distributedScheduled(ProceedingJoinPoint proceedingJoinPoint, Method targetMethod) {

        DistributedScheduled distributedScheduled = targetMethod.getAnnotation(DistributedScheduled.class);
        String lockKey = distributedScheduled.value();
        if ("".equals(lockKey)) {
            lockKey = proceedingJoinPoint.getSignature().toString();
        }

        boolean done = this.acLock.tryLockDo(lockKey, () -> {
            try {
                proceedingJoinPoint.proceed();
            } catch (Throwable var2) {
                var2.printStackTrace();
            }

        });
        if (done) {
            log.info("执行schedule：{}。", lockKey);
        } else {
            log.info("schedule：{}，已由其他client执行。", lockKey);
        }

        return null;
    }
}

