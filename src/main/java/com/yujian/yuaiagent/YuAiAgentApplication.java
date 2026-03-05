package com.yujian.yuaiagent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class YuAiAgentApplication {

    private static final Logger log = LoggerFactory.getLogger(YuAiAgentApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(YuAiAgentApplication.class, args);
    }

    /**
     * 项目启动后打印接口文档地址
     */
    @Bean
    public CommandLineRunner printApiDocUrl() {
        return args -> {
            System.out.println();
            System.out.println("========================================");
            System.out.println("  项目启动成功！");
            System.out.println("  接口文档地址：http://localhost:8123/api/doc.html");
            System.out.println("========================================");
            System.out.println();
        };
    }

}
