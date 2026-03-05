package com.yujian.yuaiagent.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 限流注解
 *
 * <p>使用示例：</p>
 * <pre>
 * {@code @RateLimit(maxRequests = 10, windowSeconds = 60)}
 * public String createUser(@RequestBody User user) { ... }
 * </pre>
 *
 * @see com.yujian.yuaiagent.aspect.RateLimitAspect
 */
@Target({ElementType.METHOD, ElementType.TYPE})  // 可应用于方法或类
@Retention(RetentionPolicy.RUNTIME)              // 运行时可见
@Documented
public @interface RateLimit {

    /**
     * 最大请求次数
     */
    int maxRequests() default 100;

    /**
     * 时间窗口（秒）
     */
    int windowSeconds() default 60;

    /**
     * 限流 key，支持 SpEL 表达式
     * 默认使用 ip 地址
     */
    String key() default "#remoteAddr";

    /**
     * 自定义错误消息
     */
    String message() default "请求过于频繁，请稍后再试";
}
