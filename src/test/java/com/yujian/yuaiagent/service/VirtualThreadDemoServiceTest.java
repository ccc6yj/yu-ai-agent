package com.yujian.yuaiagent.service;

import com.yujian.yuaiagent.service.VirtualThreadDemoService.ThreadComparisonResult;
import com.yujian.yuaiagent.service.VirtualThreadDemoService.ThreadScenario;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VirtualThreadDemoServiceTest {

    private final VirtualThreadDemoService virtualThreadDemoService = new VirtualThreadDemoService();

    @Test
    void shouldCompareIoBoundScenario() {
        ThreadComparisonResult result = virtualThreadDemoService.compare(ThreadScenario.IO_BOUND, 12, 3, 10, 1_000);

        assertEquals(ThreadScenario.IO_BOUND, result.scenario());
        assertEquals(12, result.taskCount());
        assertTrue(result.platformThreadResult().uniqueThreadCount() <= 3);
        assertFalse(result.platformThreadResult().allVirtual());
        assertTrue(result.virtualThreadResult().allVirtual());
        assertEquals(result.platformThreadResult().checksum(), result.virtualThreadResult().checksum());
    }

    @Test
    void shouldCompareCpuBoundScenario() {
        ThreadComparisonResult result = virtualThreadDemoService.compare(ThreadScenario.CPU_BOUND, 10, 4, 10, 5_000);

        assertEquals(ThreadScenario.CPU_BOUND, result.scenario());
        assertTrue(result.platformThreadResult().uniqueThreadCount() <= 4);
        assertFalse(result.platformThreadResult().allVirtual());
        assertTrue(result.virtualThreadResult().allVirtual());
        assertEquals(result.platformThreadResult().checksum(), result.virtualThreadResult().checksum());
    }
}
