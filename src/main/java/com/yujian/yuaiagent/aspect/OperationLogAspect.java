package com.yujian.yuaiagent.aspect;

import com.yujian.yuaiagent.annotation.OperationLog;
import com.yujian.yuaiagent.annotation.OperationType;
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
import java.util.Arrays;

/**
 * 操作日志注解切面
 */
@Aspect
@Component
public class OperationLogAspect {

    private static final Logger log = LoggerFactory.getLogger(OperationLogAspect.class);

    @Pointcut("@annotation(com.yujian.yuaiagent.annotation.OperationLog)")
    public void operationLogPointcut() {
    }

    @Around("operationLogPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        OperationLog operationLog = method.getAnnotation(OperationLog.class);

        // 获取请求信息
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = null;
        if (attributes != null) {
            request = attributes.getRequest();
        }

        // 记录操作开始
        long startTime = System.currentTimeMillis();
        String operationName = operationLog.operation().getDescription();
        String moduleName = operationLog.module();
        String description = operationLog.description();

        log.info("【操作日志】模块：{} | 操作：{} | 描述：{} | 方法：{}.{} | 参数：{}",
                moduleName,
                operationName,
                description,
                method.getDeclaringClass().getSimpleName(),
                method.getName(),
                Arrays.toString(joinPoint.getArgs())
        );

        try {
            Object result = joinPoint.proceed();
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            log.info("【操作日志】完成：{} - {} | 耗时：{}ms",
                    moduleName, operationName, duration);

            return result;
        } catch (Exception e) {
            log.error("【操作日志】异常：{} - {} | 错误：{}",
                    moduleName, operationName, e.getMessage());
            throw e;
        }
    }
}
