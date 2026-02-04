package com.yujian.yuaiagent.controller;

import jakarta.servlet.http.HttpServletResponse;
import reactor.core.publisher.Flux;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/ai")
public class ChatClientController {

    private static final String DEFAULT_PROMPT = "你好，介绍下你自己！请用中文回答。";

    private final ChatModel chatModel;
    private final Map<String, ChatClient> chatClients = new ConcurrentHashMap<>();

    public ChatClientController(@Qualifier("anthropicChatModel") ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    /**
     * 获取或创建指定会话的 ChatClient
     */
    private ChatClient getChatClient(String conversationId) {
        return chatClients.computeIfAbsent(conversationId, id -> {
            InMemoryChatMemory memory = new InMemoryChatMemory();
            return ChatClient.builder(chatModel)
                    .defaultAdvisors(
                            new SimpleLoggerAdvisor(),
                            new MessageChatMemoryAdvisor(memory)
                    )
                    .build();
        });
    }

    /**
     * 多轮对话 - 支持记忆上下文
     * @param message 用户消息
     * @param conversationId 会话ID，相同ID的对话会共享上下文
     */
    @GetMapping("/chat")
    public String chat(
            @RequestParam(value = "message", defaultValue = DEFAULT_PROMPT) String message,
            @RequestParam(value = "conversationId", defaultValue = "default") String conversationId) {
        return getChatClient(conversationId)
                .prompt()
                .user(message)
                .call()
                .content();
    }

    /**
     * 流式多轮对话
     */
    @GetMapping("/stream")
    public Flux<String> stream(
            @RequestParam(value = "message", defaultValue = DEFAULT_PROMPT) String message,
            @RequestParam(value = "conversationId", defaultValue = "default") String conversationId,
            HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        return getChatClient(conversationId)
                .prompt()
                .user(message)
                .stream()
                .content();
    }

    /**
     * 清除指定会话的历史记录
     */
    @GetMapping("/clear")
    public String clearHistory(@RequestParam(value = "conversationId", defaultValue = "default") String conversationId) {
        chatClients.remove(conversationId);
        return "会话 " + conversationId + " 的历史记录已清除";
    }

}
