# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

基于 Spring Boot 3.4.6 + Spring AI 的 AI Agent 学习项目，集成 Anthropic Claude 和阿里云通义千问模型。

## 构建与运行

```bash
# 构建
mvn clean install

# 运行
mvn spring-boot:run

# 测试
mvn test

# 运行单个测试类
mvn test -Dtest=YuAiAgentApplicationTests
```

## 技术栈

- **JDK 21**
- **Spring Boot 3.4.6**
- **Spring AI 1.0.0-M6** (Anthropic)
- **Spring AI Alibaba 1.0.0-M6.1** (DashScope)
- **Knife4j 4.6.0** (API 文档)
- **Hutool 5.8.37** (工具类)
- **Lombok**

## 架构结构

```
src/main/java/com/yujian/yuaiagent/
├── YuAiAgentApplication.java          # 启动类
├── config/
│   └── AiToolsConfig.java             # AI 工具函数配置 (Function Calling)
├── controller/
│   ├── HealthController.java          # 健康检查
│   ├── ChatClientController.java      # 基础聊天接口 (支持多轮对话/流式)
│   └── AdvancedChatController.java    # 高级功能接口 (Function Calling/Structured Output)
├── demo/
│   ├── invoke/                        # 不同方式调用 AI 的示例
│   │   ├── HttpAiInvoke.java          # HTTP 原生调用
│   │   ├── SdkAiInvoke.java           # SDK 调用
│   │   ├── SpringAiAiInvoke.java      # Spring AI 调用
│   │   └── OllamaAiInvoke.java        # Ollama 本地模型调用
│   └── advanced/
│       ├── WeatherFunctionModels.java # Function Calling 数据模型
│       └── BookRecommendation.java    # Structured Output 数据模型
└── TestApiKey.java                    # API Key 测试工具
```

## API 接口

| 接口 | 路径 | 说明 |
|------|------|------|
| 健康检查 | `GET /api/health` | 健康检查 |
| 基础聊天 | `GET /api/ai/chat` | 支持 conversationId 多轮对话 |
| 流式聊天 | `GET /api/ai/stream` | 流式输出 |
| 清除会话 | `GET /api/ai/clear` | 清除会话历史 |
| 天气查询 | `GET /api/ai/advanced/weather` | Function Calling 示例 |
| 图书推荐 | `GET /api/ai/advanced/book` | Structured Output 示例 (返回 JSON) |

- **API 文档**: http://localhost:8123/api/doc.html

## 配置文件

`application.yaml` 配置要点：
- 服务端口：8123
- 上下文路径：/api
- Anthropic 配置：`spring.ai.anthropic`
- DashScope 配置：`spring.ai.dashscope`

## Spring AI 核心概念

项目中的关键模式：

1. **ChatModel** - 底层模型接口，通过 `@Qualifier("anthropicChatModel")` 注入
2. **ChatClient** - 高层封装，支持链式调用和 Advisor
3. **Function Calling** - 在 `AiToolsConfig` 中定义 `@Bean` 函数，通过 `.functions("beanName")` 注册
4. **Structured Output** - 使用 `.entity(YourClass.class)` 将响应映射为 Java 对象
5. **Chat Memory** - 使用 `InMemoryChatMemory` + `MessageChatMemoryAdvisor` 实现多轮对话

详细学习文档见 `docs/SPRING_AI_LEARNING.md`
