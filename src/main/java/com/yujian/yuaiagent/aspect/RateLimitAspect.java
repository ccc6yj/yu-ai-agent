package com.yujian.yuaiagent.aspect;

import com.yujian.yuaiagent.annotation.RateLimit;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 限流注解切面
 */
@Aspect
@Component
public class RateLimitAspect {

    private static final Logger log = LoggerFactory.getLogger(RateLimitAspect.class);

    /**
     * 存储限流计数器：key -> (窗口起始时间，计数器)
     */
    private final Map<String, WindowCounter> counterMap = new ConcurrentHashMap<>();

    /**
     * 限流器内部类
     */
    private static class WindowCounter {
        long windowStart;
        AtomicInteger count;

        WindowCounter() {
            this.windowStart = System.currentTimeMillis();
            this.count = new AtomicInteger(1);
        }
    }

    @Pointcut("@annotation(com.yujian.yuaiagent.annotation.RateLimit)")
    public void rateLimitPointcut() {
    }

    @Around("rateLimitPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // 获取注解
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);

        // 如果方法上没有，检查类上
        if (rateLimit == null) {
            rateLimit = joinPoint.getTarget().getClass().getAnnotation(RateLimit.class);
        }

        if (rateLimit == null) {
            return joinPoint.proceed();
        }

        // 获取请求
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = attributes.getRequest();
        String remoteAddr = request.getRemoteAddr();

        // 构建限流 key
        String key = rateLimit.key().replace("#remoteAddr", remoteAddr);

        return doRateLimit(key, rateLimit, joinPoint);
    }

    private Object doRateLimit(String key, RateLimit rateLimit, ProceedingJoinPoint joinPoint) throws Throwable {
        long now = System.currentTimeMillis();
        long windowMillis = rateLimit.windowSeconds() * 1000L;

        WindowCounter counter = counterMap.computeIfAbsent(key, k -> new WindowCounter());

        synchronized (counter) {
            // 检查是否超出时间窗口
            if (now - counter.windowStart >= windowMillis) {
                // 重置窗口
                counter.windowStart = now;
                counter.count.set(1);
            } else {
                // 在窗口内，检查是否超限
                int currentCount = counter.count.incrementAndGet();
                if (currentCount > rateLimit.maxRequests()) {
                    log.warn("限流触发：key={}, maxRequests={}, windowSeconds={}",
                            key, rateLimit.maxRequests(), rateLimit.windowSeconds());
                    throw new RuntimeException(rateLimit.message());
                }
            }
        }

        return joinPoint.proceed();
    }
}
