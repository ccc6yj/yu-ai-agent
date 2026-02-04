# Spring AI 学习指南

## 学习路线图

```
第一阶段：基础入门
    ├── 1.1 核心概念理解
    ├── 1.2 ChatModel 基本使用
    └── 1.3 ChatClient 链式调用

第二阶段：对话增强
    ├── 2.1 System Prompt 系统提示词
    ├── 2.2 Prompt Template 提示词模板
    └── 2.3 Chat Memory 多轮对话记忆

第三阶段：高级功能
    ├── 3.1 Function Calling 函数调用
    ├── 3.2 Structured Output 结构化输出
    └── 3.3 流式响应处理

第四阶段：RAG 检索增强
    ├── 4.1 Embedding 文本向量化
    ├── 4.2 Vector Store 向量数据库
    └── 4.3 RAG 完整实现

第五阶段：Agent 智能体
    ├── 5.1 Agent 概念与设计
    ├── 5.2 Tool 工具定义
    └── 5.3 多 Agent 协作
```

---

## 第一阶段：基础入门

### 1.1 核心概念

| 概念 | 说明 |
|------|------|
| **ChatModel** | 底层接口，直接调用 AI 模型 |
| **ChatClient** | 高层封装，支持链式调用、Advisor |
| **Prompt** | 提示词，包含用户输入和配置 |
| **Message** | 消息，分为 UserMessage、AssistantMessage、SystemMessage |
| **Advisor** | 拦截器，可在请求前后添加逻辑（日志、记忆、RAG） |

### 1.2 ChatModel 基本使用

```java
@Autowired
private ChatModel chatModel;

// 最简单的调用
String response = chatModel.call("你好");

// 使用 Prompt 对象
Prompt prompt = new Prompt("你好，请用中文回答");
ChatResponse response = chatModel.call(prompt);
String text = response.getResult().getOutput().getText();
```

### 1.3 ChatClient 链式调用（推荐）

```java
// 构建 ChatClient
ChatClient chatClient = ChatClient.builder(chatModel).build();

// 简单调用
String response = chatClient.prompt("你好").call().content();

// 链式调用
String response = chatClient.prompt()
        .system("你是一个 Java 专家")
        .user("什么是 Spring Boot?")
        .call()
        .content();
```

---

## 第二阶段：对话增强

### 2.1 System Prompt 系统提示词

系统提示词用于设定 AI 的角色、行为规范。

```java
ChatClient chatClient = ChatClient.builder(chatModel)
        .defaultSystem("你是一个专业的 Java 开发助手，回答简洁专业")
        .build();

// 或者在调用时设置
String response = chatClient.prompt()
        .system("你是一个幽默的程序员，喜欢用代码举例")
        .user("解释什么是递归")
        .call()
        .content();
```

### 2.2 Prompt Template 提示词模板

使用模板管理提示词，支持变量替换。

```java
// 定义模板
String template = """
        你是一个{role}专家。
        请用{language}回答以下问题：
        {question}
        """;

PromptTemplate promptTemplate = new PromptTemplate(template);
Prompt prompt = promptTemplate.create(Map.of(
        "role", "Java",
        "language", "中文",
        "question", "什么是 Spring AOP?"
));

String response = chatClient.prompt(prompt).call().content();
```

### 2.3 Chat Memory 多轮对话记忆

让 AI 记住对话历史，实现连续对话。

```java
// 创建内存存储
InMemoryChatMemory memory = new InMemoryChatMemory();

// 构建带记忆的 ChatClient
ChatClient chatClient = ChatClient.builder(chatModel)
        .defaultAdvisors(new MessageChatMemoryAdvisor(memory))
        .build();

// 多轮对话
chatClient.prompt().user("我叫小明").call().content();
chatClient.prompt().user("我叫什么名字？").call().content();  // AI 会记住
```

---

## 第三阶段：高级功能

### 3.1 Function Calling 函数调用

让 AI 调用你定义的 Java 方法。

```java
// 1. 定义函数
@Bean
@Description("获取指定城市的天气信息")
public Function<WeatherRequest, WeatherResponse> getWeather() {
    return request -> {
        // 调用天气 API
        return new WeatherResponse(request.city(), "晴天", 25);
    };
}

// 2. 请求/响应对象
record WeatherRequest(String city) {}
record WeatherResponse(String city, String weather, int temperature) {}

// 3. 调用时注册函数
String response = chatClient.prompt()
        .user("北京今天天气怎么样？")
        .functions("getWeather")
        .call()
        .content();
```

### 3.2 Structured Output 结构化输出

让 AI 返回结构化的 Java 对象。

```java
// 定义返回类型
record BookInfo(String title, String author, int year) {}

// 调用并获取结构化结果
BookInfo book = chatClient.prompt()
        .user("推荐一本 Java 入门书籍")
        .call()
        .entity(BookInfo.class);

System.out.println(book.title());  // 输出书名
```

### 3.3 流式响应处理

实现打字机效果。

```java
// 返回 Flux 流
Flux<String> stream = chatClient.prompt()
        .user("写一首关于春天的诗")
        .stream()
        .content();

// 订阅处理
stream.subscribe(
        chunk -> System.out.print(chunk),  // 逐字输出
        error -> error.printStackTrace(),
        () -> System.out.println("\n完成")
);
```

---

## 第四阶段：RAG 检索增强

### 4.1 Embedding 文本向量化

将文本转换为向量，用于语义搜索。

```java
@Autowired
private EmbeddingModel embeddingModel;

// 单个文本向量化
float[] vector = embeddingModel.embed("Spring Boot 是什么？");

// 批量向量化
List<float[]> vectors = embeddingModel.embed(List.of("文本1", "文本2"));
```

### 4.2 Vector Store 向量数据库

存储和检索向量。

```java
@Autowired
private VectorStore vectorStore;

// 存储文档
List<Document> documents = List.of(
        new Document("Spring Boot 简化了 Spring 应用开发"),
        new Document("Spring AI 提供 AI 集成能力")
);
vectorStore.add(documents);

// 相似度搜索
List<Document> results = vectorStore.similaritySearch("什么是 Spring Boot？");
```

### 4.3 RAG 完整实现

结合检索和生成。

```java
// 使用 QuestionAnswerAdvisor 实现 RAG
ChatClient chatClient = ChatClient.builder(chatModel)
        .defaultAdvisors(new QuestionAnswerAdvisor(vectorStore))
        .build();

// 查询时自动检索相关文档
String response = chatClient.prompt()
        .user("Spring Boot 有什么优势？")
        .call()
        .content();
```

---

## 第五阶段：Agent 智能体

### 5.1 Agent 概念

Agent = LLM + Tools + Memory + Planning

```
┌─────────────────────────────────────┐
│              Agent                  │
├─────────────────────────────────────┤
│  ┌─────────┐  ┌─────────┐          │
│  │   LLM   │  │ Memory  │          │
│  └─────────┘  └─────────┘          │
│  ┌─────────────────────────────┐   │
│  │          Tools              │   │
│  │  ┌─────┐ ┌─────┐ ┌─────┐   │   │
│  │  │查天气│ │查数据│ │发邮件│   │   │
│  │  └─────┘ └─────┘ └─────┘   │   │
│  └─────────────────────────────┘   │
└─────────────────────────────────────┘
```

### 5.2 Tool 工具定义

```java
@Component
public class MyTools {

    @Tool(description = "搜索用户信息")
    public User searchUser(@Param("userId") String userId) {
        return userService.findById(userId);
    }

    @Tool(description = "发送邮件通知")
    public String sendEmail(
            @Param("to") String to,
            @Param("subject") String subject,
            @Param("content") String content) {
        emailService.send(to, subject, content);
        return "邮件发送成功";
    }
}
```

### 5.3 多 Agent 协作

```java
// 规划 Agent
ChatClient plannerAgent = ChatClient.builder(chatModel)
        .defaultSystem("你是一个任务规划专家，负责分解复杂任务")
        .build();

// 执行 Agent
ChatClient executorAgent = ChatClient.builder(chatModel)
        .defaultSystem("你是一个任务执行者，负责完成具体任务")
        .defaultFunctions("searchUser", "sendEmail")
        .build();

// 协作流程
String plan = plannerAgent.prompt()
        .user("给用户 123 发送生日祝福邮件")
        .call()
        .content();

String result = executorAgent.prompt()
        .user(plan)
        .call()
        .content();
```

---

## 实践项目建议

| 阶段 | 项目 | 涉及知识点 |
|------|------|-----------|
| 1 | 简单问答机器人 | ChatClient、基本调用 |
| 2 | 角色扮演聊天 | System Prompt、多轮对话 |
| 3 | 智能客服 | Function Calling、结构化输出 |
| 4 | 知识库问答 | RAG、向量数据库 |
| 5 | AI 助手 | Agent、多工具协作 |

---

## 学习资源

- [Spring AI 官方文档](https://docs.spring.io/spring-ai/reference/)
- [Spring AI GitHub](https://github.com/spring-projects/spring-ai)
- [Spring AI Alibaba](https://github.com/alibaba/spring-ai-alibaba)
