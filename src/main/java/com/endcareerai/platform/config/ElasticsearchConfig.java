package com.endcareerai.platform.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.endcareerai.platform.es")
public class ElasticsearchConfig {
}
