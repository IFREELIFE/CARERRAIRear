package com.endcareerai.platform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * EndCareerAi 智能求职匹配平台启动类
 */
@SpringBootApplication
@MapperScan("com.endcareerai.platform.mapper")
public class EndCareerAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(EndCareerAiApplication.class, args);
    }
}
