package com.endcareerai.platform.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

/**
 * Elasticsearch 配置类
 * 启用 Elasticsearch Repository 扫描，扫描 es 包下的所有 Repository 接口
 */
@Configuration
@EnableElasticsearchRepositories(basePackages = "com.endcareerai.platform.es")
public class ElasticsearchConfig {
}
