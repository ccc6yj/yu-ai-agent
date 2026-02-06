package com.yujian.yuaiagent.controller;

import com.yujian.yuaiagent.demo.advanced.BookRecommendation;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai/advanced")
public class AdvancedChatController {

    private final ChatClient chatClient;

    public AdvancedChatController(@Qualifier("anthropicChatModel") ChatModel chatModel) {
        this.chatClient = ChatClient.builder(chatModel).build();
    }

    /**
     * Function Calling 示例：模型会调用 getWeather 函数获取天气
     */
    @GetMapping("/weather")
    public String weather(@RequestParam(defaultValue = "北京") String city) {
        return chatClient.prompt()
                .system("你是天气助手，先调用函数获取实时天气，再用一句中文总结。")
                .user("请查询" + city + "今天天气")
                .functions("getWeather")
                .call()
                .content();
    }

    /**
     * Structured Output 示例：将模型输出映射成 Java 对象
     */
    @GetMapping("/book")
    public BookRecommendation book(@RequestParam(defaultValue = "Spring Boot") String topic) {
        return chatClient.prompt()
                .system("你是资深技术图书顾问，回答要准确精炼。")
                .user("推荐一本关于" + topic + "的书，并给出理由和 3 条关键收获。")
                .call()
                .entity(BookRecommendation.class);
    }
}
