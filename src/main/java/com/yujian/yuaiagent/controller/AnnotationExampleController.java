package com.yujian.yuaiagent.controller;

import com.yujian.yuaiagent.annotation.OperationLog;
import com.yujian.yuaiagent.annotation.OperationType;
import com.yujian.yuaiagent.annotation.RateLimit;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 自定义注解使用示例 Controller
 *
 * <p>演示如何使用 @RateLimit 和 @OperationLog 注解</p>
 */
@RestController
@RequestMapping("/demo/annotation")
public class AnnotationExampleController {

    /**
     * 限流示例
     *
     * 访问地址：GET /api/demo/annotation/rate-limit-test
     * 效果：60 秒内最多访问 10 次，超出会抛出异常
     */
    @GetMapping("/rate-limit-test")
    @RateLimit(maxRequests = 10, windowSeconds = 60, message = "访问太频繁啦，请等一等再试~")
    public Map<String, Object> rateLimitTest() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "请求成功");
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    /**
     * 操作日志示例 - 创建用户
     *
     * 访问地址：POST /api/demo/annotation/user
     */
    @PostMapping("/user")
    @OperationLog(module = "用户管理", operation = OperationType.CREATE, description = "创建新用户")
    @RateLimit(maxRequests = 5, windowSeconds = 60)
    public Map<String, Object> createUser(@RequestParam String username) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "用户创建成功：" + username);
        result.put("data", Map.of("username", username, "id", System.currentTimeMillis()));
        return result;
    }

    /**
     * 操作日志示例 - 查询用户
     *
     * 访问地址：GET /api/demo/annotation/user/123
     */
    @GetMapping("/user/{id}")
    @OperationLog(module = "用户管理", operation = OperationType.QUERY, description = "查询用户详情")
    public Map<String, Object> getUser(@PathVariable String id) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "查询成功");
        result.put("data", Map.of("id", id, "username", "user_" + id));
        return result;
    }

    /**
     * 组合注解示例
     *
     * 同时使用 @RateLimit 和 @OperationLog
     * 访问地址：GET /api/demo/annotation/export
     */
    @GetMapping("/export")
    @RateLimit(maxRequests = 3, windowSeconds = 300, message = "导出操作频繁，请 5 分钟后再试")
    @OperationLog(module = "数据管理", operation = OperationType.EXPORT, description = "导出数据")
    public Map<String, Object> export() throws InterruptedException {
        // 模拟耗时操作
        Thread.sleep(1000);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "导出成功");
        result.put("data", Map.of("rows", 1000, "file", "export_2024.csv"));
        return result;
    }
}
