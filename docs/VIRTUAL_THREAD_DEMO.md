# 虚拟线程 Demo

这个 demo 用来对比两种执行方式：

- 固定大小的平台线程池 `Executors.newFixedThreadPool(...)`
- 虚拟线程执行器 `Executors.newThreadPerTaskExecutor(Thread.ofVirtual().factory())`

## 启动项目

```bash
./mvnw spring-boot:run
```

项目启动后，访问地址前缀为 `http://localhost:8123/api`。

## 1. IO 阻塞场景

```bash
curl "http://localhost:8123/api/demo/threads/compare?scenario=io&taskCount=1000&sleepMillis=200&platformThreads=50"
```

这个实验模拟大量任务都在等待，例如：

- 数据库查询等待
- HTTP 调用等待
- 文件读写等待

重点观察：

- `platformThreadResult.durationMillis`
- `virtualThreadResult.durationMillis`
- `platformThreadResult.uniqueThreadCount`
- `virtualThreadResult.uniqueThreadCount`

通常你会看到：

- 平台线程会受线程池大小限制，任务需要排队
- 虚拟线程可以承载更多阻塞任务，并发写法更直接

## 2. CPU 密集场景

```bash
curl "http://localhost:8123/api/demo/threads/compare?scenario=cpu&taskCount=200&primeLimit=30000&platformThreads=8"
```

这个实验模拟纯计算任务。通常你会看到：

- 虚拟线程不一定更快
- CPU 密集任务的瓶颈仍然是 CPU 核心数
- 虚拟线程主要优势不是“让计算变快”，而是“让高并发阻塞式代码更容易写”

## 如何理解结果

如果是 `IO` 场景：

- 虚拟线程更容易在高并发下保持较低调度成本
- 更适合“一请求一线程”的直观编程模型

如果是 `CPU` 场景：

- 平台线程和虚拟线程的差距通常不会特别大
- 真正决定性能的是算法、CPU 核数、任务切分方式

## 代码位置

- `src/main/java/com/yujian/yuaiagent/controller/VirtualThreadDemoController.java`
- `src/main/java/com/yujian/yuaiagent/service/VirtualThreadDemoService.java`
