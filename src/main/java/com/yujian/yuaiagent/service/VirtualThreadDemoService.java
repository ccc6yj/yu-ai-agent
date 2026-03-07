package com.yujian.yuaiagent.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.function.IntFunction;

/**
 * 虚拟线程与平台线程对比示例
 */
@Service
public class VirtualThreadDemoService {

    public ThreadComparisonResult compare(ThreadScenario scenario,
                                          int taskCount,
                                          int platformThreadCount,
                                          int sleepMillis,
                                          int primeLimit) {
        // 根据场景构造任务：IO 场景模拟阻塞等待，CPU 场景模拟纯计算
        IntFunction<TaskSnapshot> taskFactory = switch (scenario) {
            case IO_BOUND -> taskIndex -> runSleepTask(taskIndex, sleepMillis);
            case CPU_BOUND -> taskIndex -> runPrimeTask(taskIndex, primeLimit);
        };

        ExecutorSummary platformThreadResult = runScenario(
                "platform",
                Executors.newFixedThreadPool(platformThreadCount, platformThreadFactory()),
                taskCount,
                taskFactory
        );
        ExecutorSummary virtualThreadResult = runScenario(
                "virtual",
                Executors.newThreadPerTaskExecutor(virtualThreadFactory()),
                taskCount,
                taskFactory
        );

        return new ThreadComparisonResult(
                scenario,
                taskCount,
                platformThreadCount,
                sleepMillis,
                primeLimit,
                platformThreadResult,
                virtualThreadResult,
                buildConclusion(scenario, platformThreadResult, virtualThreadResult)
        );
    }

    private ExecutorSummary runScenario(String executorType,
                                        ExecutorService executorService,
                                        int taskCount,
                                        IntFunction<TaskSnapshot> taskFactory) {
        long startNanos = System.nanoTime();
        try (executorService) {
            List<Future<TaskSnapshot>> futures = new ArrayList<>(taskCount);
            for (int taskIndex = 0; taskIndex < taskCount; taskIndex++) {
                int currentTaskIndex = taskIndex;
                futures.add(executorService.submit(() -> taskFactory.apply(currentTaskIndex)));
            }

            List<TaskSnapshot> snapshots = new ArrayList<>(taskCount);
            for (Future<TaskSnapshot> future : futures) {
                snapshots.add(future.get());
            }

            long durationMillis = (System.nanoTime() - startNanos) / 1_000_000;
            // 统计实际参与执行的线程数，便于观察线程复用和扩展能力
            Set<String> uniqueThreadNames = snapshots.stream()
                    .map(TaskSnapshot::threadName)
                    .collect(java.util.stream.Collectors.toSet());
            List<String> sampleThreadNames = uniqueThreadNames.stream()
                    .sorted(Comparator.naturalOrder())
                    .limit(8)
                    .toList();
            boolean allVirtual = snapshots.stream().allMatch(TaskSnapshot::virtual);
            // checksum 用来确认两种执行器跑出的任务结果一致，避免只比较耗时
            long checksum = snapshots.stream().mapToLong(TaskSnapshot::payload).sum();

            return new ExecutorSummary(
                    executorType,
                    durationMillis,
                    uniqueThreadNames.size(),
                    allVirtual,
                    sampleThreadNames,
                    checksum
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("线程对比任务被中断", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("线程对比任务执行失败", e.getCause());
        }
    }

    private TaskSnapshot runSleepTask(int taskIndex, int sleepMillis) {
        try {
            // 用 sleep 模拟典型阻塞式 IO 等待
            Thread.sleep(sleepMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("IO 模拟任务被中断，taskIndex=" + taskIndex, e);
        }
        Thread currentThread = Thread.currentThread();
        return new TaskSnapshot(currentThread.getName(), currentThread.isVirtual(), sleepMillis);
    }

    private TaskSnapshot runPrimeTask(int taskIndex, int primeLimit) {
        // 用重复计算质数模拟 CPU 密集型任务
        long primeSum = sumPrimesUpTo(primeLimit);
        Thread currentThread = Thread.currentThread();
        return new TaskSnapshot(currentThread.getName(), currentThread.isVirtual(), primeSum + taskIndex);
    }

    private long sumPrimesUpTo(int limit) {
        long sum = 0L;
        for (int number = 2; number <= limit; number++) {
            if (isPrime(number)) {
                sum += number;
            }
        }
        return sum;
    }

    private boolean isPrime(int number) {
        for (int factor = 2; factor * factor <= number; factor++) {
            if (number % factor == 0) {
                return false;
            }
        }
        return true;
    }

    private ThreadFactory platformThreadFactory() {
        return Thread.ofPlatform().name("platform-demo-", 0).factory();
    }

    private ThreadFactory virtualThreadFactory() {
        return Thread.ofVirtual().name("virtual-demo-", 0).factory();
    }

    private String buildConclusion(ThreadScenario scenario,
                                   ExecutorSummary platformThreadResult,
                                   ExecutorSummary virtualThreadResult) {
        return switch (scenario) {
            case IO_BOUND -> String.format(
                    Locale.ROOT,
                    "IO 阻塞场景下，虚拟线程通常更适合高并发等待型任务。本次平台线程耗时 %d ms，虚拟线程耗时 %d ms。",
                    platformThreadResult.durationMillis(),
                    virtualThreadResult.durationMillis()
            );
            case CPU_BOUND -> String.format(
                    Locale.ROOT,
                    "CPU 密集场景下，虚拟线程不会让 CPU 计算本身变快，更适合通过简化并发编程模型提升可维护性。本次平台线程耗时 %d ms，虚拟线程耗时 %d ms。",
                    platformThreadResult.durationMillis(),
                    virtualThreadResult.durationMillis()
            );
        };
    }

    public enum ThreadScenario {
        IO_BOUND,
        CPU_BOUND;

        public static ThreadScenario from(String value) {
            return switch (value.toLowerCase(Locale.ROOT)) {
                case "io", "io_bound", "blocking" -> IO_BOUND;
                case "cpu", "cpu_bound" -> CPU_BOUND;
                default -> throw new IllegalArgumentException("不支持的场景类型: " + value);
            };
        }
    }

    private record TaskSnapshot(String threadName, boolean virtual, long payload) {
    }

    public record ThreadComparisonResult(ThreadScenario scenario,
                                         int taskCount,
                                         int platformThreadCount,
                                         int sleepMillis,
                                         int primeLimit,
                                         ExecutorSummary platformThreadResult,
                                         ExecutorSummary virtualThreadResult,
                                         String conclusion) {
    }

    public record ExecutorSummary(String executorType,
                                  long durationMillis,
                                  int uniqueThreadCount,
                                  boolean allVirtual,
                                  List<String> sampleThreadNames,
                                  long checksum) {
    }
}
