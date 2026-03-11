package com.endcareerai.platform.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * LLM API 配置类
 * 支持 OpenAI 兼容接口（OpenAI / DeepSeek / Azure OpenAI 等）
 */
@Configuration
public class LlmConfig {

    @Value("${llm.api-key:}")
    private String apiKey;

    @Value("${llm.connect-timeout:10}")
    private int connectTimeout;

    @Value("${llm.read-timeout:60}")
    private int readTimeout;

    @Bean("llmRestTemplate")
    public RestTemplate llmRestTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(connectTimeout))
                .setReadTimeout(Duration.ofSeconds(readTimeout))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .build();
    }
}
