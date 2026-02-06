package com.yujian.yuaiagent.demo.advanced;

import java.util.List;

/**
 * Structured Output 示例：让模型返回固定结构
 */
public record BookRecommendation(String title, String author, String reason, List<String> keyPoints) {
}
