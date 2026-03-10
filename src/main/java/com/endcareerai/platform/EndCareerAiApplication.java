package com.endcareerai.platform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.endcareerai.platform.mapper")
public class EndCareerAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(EndCareerAiApplication.class, args);
    }
}
