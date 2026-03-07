package com.yujian.yuaiagent.controller;

import com.yujian.yuaiagent.service.VirtualThreadDemoService;
import com.yujian.yuaiagent.service.VirtualThreadDemoService.ThreadComparisonResult;
import com.yujian.yuaiagent.service.VirtualThreadDemoService.ThreadScenario;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 虚拟线程学习示例接口
 */
@RestController
@RequestMapping("/demo/threads")
public class VirtualThreadDemoController {

    private final VirtualThreadDemoService virtualThreadDemoService;

    public VirtualThreadDemoController(VirtualThreadDemoService virtualThreadDemoService) {
        this.virtualThreadDemoService = virtualThreadDemoService;
    }

    /**
     * 访问示例：
     * GET /api/demo/threads/compare?scenario=io&taskCount=1000&sleepMillis=200&platformThreads=50
     * GET /api/demo/threads/compare?scenario=cpu&taskCount=200&primeLimit=30000&platformThreads=8
     */
    @GetMapping("/compare")
    public ThreadComparisonResult compare(@RequestParam(defaultValue = "io") String scenario,
                                          @RequestParam(defaultValue = "1000") int taskCount,
                                          @RequestParam(defaultValue = "50") int platformThreads,
                                          @RequestParam(defaultValue = "200") int sleepMillis,
                                          @RequestParam(defaultValue = "30000") int primeLimit) {
        // 先做边界校验，避免一次请求把机器压得过重
        validate(taskCount, platformThreads, sleepMillis, primeLimit);
        ThreadScenario threadScenario = ThreadScenario.from(scenario);
        return virtualThreadDemoService.compare(threadScenario, taskCount, platformThreads, sleepMillis, primeLimit);
    }

    private void validate(int taskCount, int platformThreads, int sleepMillis, int primeLimit) {
        if (taskCount < 1 || taskCount > 20_000) {
            throw new IllegalArgumentException("taskCount 取值范围应为 1 - 20000");
        }
        if (platformThreads < 1 || platformThreads > 500) {
            throw new IllegalArgumentException("platformThreads 取值范围应为 1 - 500");
        }
        if (sleepMillis < 1 || sleepMillis > 10_000) {
            throw new IllegalArgumentException("sleepMillis 取值范围应为 1 - 10000");
        }
        if (primeLimit < 100 || primeLimit > 200_000) {
            throw new IllegalArgumentException("primeLimit 取值范围应为 100 - 200000");
        }
    }
}
