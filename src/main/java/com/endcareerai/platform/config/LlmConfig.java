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
 * 支持 OpenAI 兼容接口（OpenAI / DeepSeek / Azure OpenAI / 通义千问等）
 * 配置 RestTemplate 的超时时间和默认 Authorization Header
 */
@Configuration
public class LlmConfig {

    // TODO: 请通过环境变量 LLM_API_KEY 注入你的 LLM API 密钥
    @Value("${llm.api-key:}")
    private String apiKey;

    // TODO: 如需调整连接超时，请修改此值（单位：秒）
    @Value("${llm.connect-timeout:10}")
    private int connectTimeout;

    // TODO: 如需调整读取超时，请修改此值（单位：秒）
    @Value("${llm.read-timeout:60}")
    private int readTimeout;

    /**
     * 创建 LLM 专用的 RestTemplate 实例
     * 预配置 JSON Content-Type、Bearer Token Authorization Header 和超时时间
     *
     * @param builder RestTemplateBuilder
     * @return 配置好的 RestTemplate 实例
     */
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
