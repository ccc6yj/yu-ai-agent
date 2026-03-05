package com.yujian.yuaiagent.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解
 *
 * <p>使用示例：</p>
 * <pre>
 * {@code @OperationLog(module = "用户管理", operation = "创建用户")}
 * public String createUser(@RequestBody User user) { ... }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

    /**
     * 模块名称
     */
    String module() default "";

    /**
     * 操作类型
     */
    OperationType operation() default OperationType.OTHER;

    /**
     * 操作描述
     */
    String description() default "";
}
