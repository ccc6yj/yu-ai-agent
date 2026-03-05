# 自定义注解学习指南

## 概述

本项目创建了两种实用的自定义注解：

1. **@RateLimit** - 限流注解，基于滑动窗口算法
2. **@OperationLog** - 操作日志注解，记录方法执行信息

两者都通过 **AOP 切面** 实现，采用声明式编程，无需修改业务代码。

---

## 注解结构

```
src/main/java/com/yujian/yuaiagent/
├── annotation/
│   ├── RateLimit.java          # 限流注解定义
│   ├── OperationLog.java       # 操作日志注解定义
│   └── OperationType.java      # 操作类型枚举
├── aspect/
│   ├── RateLimitAspect.java    # 限流切面实现
│   └── OperationLogAspect.java # 操作日志切面实现
└── controller/
    └── AnnotationExampleController.java  # 使用示例
```

---

## 1. 注解定义 (@interface)

### 1.1 @RateLimit 限流注解

```java
@Target({ElementType.METHOD, ElementType.TYPE})  // 应用位置
@Retention(RetentionPolicy.RUNTIME)              // 保留策略
@Documented
public @interface RateLimit {
    int maxRequests() default 100;      // 属性：最大请求数
    int windowSeconds() default 60;     // 属性：时间窗口
    String key() default "#remoteAddr"; // 属性：限流键
    String message() default "...";     // 属性：错误消息
}
```

**关键元注解解释：**

| 元注解 | 作用 | 可选值 |
|--------|------|--------|
| `@Target` | 指定注解应用位置 | METHOD, TYPE, FIELD, PARAMETER 等 |
| `@Retention` | 指定注解保留策略 | SOURCE, CLASS, RUNTIME |
| `@Documented` | 是否包含在 JavaDoc 中 | - |
| `@Inherited` | 子类是否继承父类注解 | - |

**Retention 策略说明：**
- `SOURCE` - 仅在源代码中保留，编译时丢弃（如 @Override）
- `CLASS` - 编译到 class 文件，运行时不可见（默认策略）
- `RUNTIME` - 运行时可见，可通过反射获取（AOP 需要此策略）

### 1.2 @OperationLog 操作日志注解

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {
    String module() default "";           // 模块名
    OperationType operation() default OTHER; // 枚举类型属性
    String description() default "";      // 描述
}
```

**枚举类型属性示例：**

```java
public enum OperationType {
    CREATE("创建"), UPDATE("更新"), DELETE("删除"),
    QUERY("查询"), IMPORT("导入"), EXPORT("导出"), OTHER("其他");
}
```

---

## 2. AOP 切面实现

### 2.1 核心概念

| 术语 | 英文 | 说明 |
|------|------|------|
| 切面 | Aspect | 横切关注点的模块化（如日志、事务） |
| 连接点 | JoinPoint | 程序执行的点（方法调用、异常抛出） |
| 切入点 | Pointcut | 匹配连接点的谓词 |
| 通知 | Advice | 切面在连接点执行的动作 |
| 织入 | Weaving | 将切面应用到目标对象 |

### 2.2 通知类型

| 类型 | 注解 | 执行时机 |
|------|------|----------|
| 环绕通知 | `@Around` | 方法执行前后（最强大） |
| 前置通知 | `@Before` | 方法执行前 |
| 后置通知 | `@After` | 方法执行后（无论是否异常） |
| 返回通知 | `@AfterReturning` | 方法正常返回后 |
| 异常通知 | `@AfterThrowing` | 方法抛出异常后 |

### 2.3 @RateLimitAspect 实现要点

```java
@Aspect
@Component
public class RateLimitAspect {

    // 1. 定义切入点：匹配带有 @RateLimit 注解的方法
    @Pointcut("@annotation(com.yujian.yuaiagent.annotation.RateLimit)")
    public void rateLimitPointcut() {}

    // 2. 环绕通知：在方法执行前后进行限流检查
    @Around("rateLimitPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取注解
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);

        // 获取请求信息
        HttpServletRequest request = ((ServletRequestAttributes)
            RequestContextHolder.getRequestAttributes()).getRequest();

        // 执行限流逻辑
        return doRateLimit(key, rateLimit, joinPoint);
    }

    // 3. 限流核心逻辑（滑动窗口算法）
    private Object doRateLimit(String key, RateLimit rateLimit,
                               ProceedingJoinPoint joinPoint) throws Throwable {
        // ... 限流实现
        return joinPoint.proceed();  // 执行目标方法
    }
}
```

**关键点：**
- `ProceedingJoinPoint.proceed()` - 执行目标方法
- `MethodSignature.getMethod()` - 获取被代理的方法
- `RequestContextHolder` - 在任意位置获取 HttpServletRequest

### 2.4 @OperationLogAspect 实现要点

```java
@Aspect
@Component
public class OperationLogAspect {

    @Around("operationLogPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 1. 记录开始时间
        long startTime = System.currentTimeMillis();

        // 2. 获取注解信息
        OperationLog log = method.getAnnotation(OperationLog.class);

        // 3. 记录请求参数
        log.info("参数：{}", Arrays.toString(joinPoint.getArgs()));

        try {
            // 4. 执行目标方法
            Object result = joinPoint.proceed();

            // 5. 记录执行结果
            log.info("耗时：{}ms", System.currentTimeMillis() - startTime);
            return result;
        } catch (Exception e) {
            // 6. 记录异常
            log.error("错误：{}", e.getMessage());
            throw e;
        }
    }
}
```

---

## 3. 使用方式

### 3.1 限流示例

```java
@GetMapping("/rate-limit-test")
@RateLimit(maxRequests = 10, windowSeconds = 60)
public Map<String, Object> test() {
    return Map.of("msg", "success");
}

// 测试：60 秒内访问超过 10 次会抛出异常
// curl http://localhost:8123/api/demo/annotation/rate-limit-test
```

### 3.2 操作日志示例

```java
@PostMapping("/user")
@OperationLog(module = "用户管理", operation = OperationType.CREATE)
public Map<String, Object> createUser(@RequestParam String username) {
    // 业务逻辑
}

// 日志输出：
// 【操作日志】模块：用户管理 | 操作：创建 | 方法：AnnotationExampleController.createUser
```

### 3.3 组合使用

```java
@GetMapping("/export")
@RateLimit(maxRequests = 3, windowSeconds = 300)
@OperationLog(module = "数据管理", operation = OperationType.EXPORT)
public Map<String, Object> export() {
    // 既有频率限制，又记录操作日志
}
```

---

## 4. 测试接口

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 限流测试 | GET | `/api/demo/annotation/rate-limit-test` | 快速访问 10 次触发限流 |
| 创建用户 | POST | `/api/demo/annotation/user?username=xxx` | 测试操作日志 + 限流 |
| 查询用户 | GET | `/api/demo/annotation/user/{id}` | 测试操作日志 |
| 数据导出 | GET | `/api/demo/annotation/export` | 测试组合注解 |

---

## 5. 扩展练习

### 5.1 添加新的注解属性

例如给 `@RateLimit` 添加 `skipException` 属性，指定哪些异常不触发限流：

```java
@RateLimit(maxRequests = 10, skipException = {ResourceNotFoundException.class})
```

### 5.2 集成 Redis 实现分布式限流

将 `Map<String, WindowCounter>` 替换为 Redis + Lua 脚本：

```java
// 使用 Redisson 或 StringRedisTemplate
String lua = "local count = redis.call('INCR', KEYS[1]) ...";
```

### 5.3 异步记录操作日志

使用 `@Async` 异步记录日志，避免阻塞主线程：

```java
@Async
public void saveLog(OperationLog log) {
    // 写入数据库
}
```

---

## 6. 常见问题

### Q1: 为什么注解要用 RUNTIME 保留策略？
A: AOP 通过反射获取注解信息，编译时或运行时早期的注解会被丢弃，只有 RUNTIME 策略才能在运行时通过 `method.getAnnotation()` 获取。

### Q2: @Around 和 @Before 有什么区别？
A: `@Around` 可以控制目标方法是否执行（通过 `proceed()`），还能修改返回值和捕获异常。`@Before` 只能在方法执行前运行，无法干预执行。

### Q3: 为什么切面类要加 @Component 注解？
A: Spring 需要管理切面类的生命周期，不加 `@Component` 切面不会被注册，AOP 也就不会生效。

### Q4: 自调用时注解为什么会失效？
A: Spring AOP 基于代理实现，同类方法调用不会经过代理。解决方法：
1. 使用 `AopContext.currentProxy()` 获取代理对象
2. 将方法移到另一个 Service/Controller
