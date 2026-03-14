package com.endcareerai.platform.config;

import com.endcareerai.platform.common.Constants;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置类
 * 定义 LLM 任务交换机、队列、绑定关系和消息序列化方式（JSON）
 */
@Configuration
public class RabbitMQConfig {

    /**
     * 定义 LLM 任务直连交换机
     *
     * @return DirectExchange 实例
     */
    @Bean
    public DirectExchange llmExchange() {
        return new DirectExchange(Constants.MQ_EXCHANGE_LLM);
    }

    /**
     * 定义 LLM 任务持久化队列
     *
     * @return Queue 实例
     */
    @Bean
    public Queue llmTaskQueue() {
        return QueueBuilder.durable(Constants.MQ_QUEUE_LLM_TASK).build();
    }

    /**
     * 将 LLM 任务队列绑定到交换机（通过路由键）
     *
     * @param llmTaskQueue LLM 任务队列
     * @param llmExchange  LLM 交换机
     * @return 绑定关系
     */
    @Bean
    public Binding llmBinding(Queue llmTaskQueue, DirectExchange llmExchange) {
        return BindingBuilder.bind(llmTaskQueue).to(llmExchange).with(Constants.MQ_ROUTING_KEY_LLM);
    }

    /**
     * 配置 JSON 消息转换器（使用 Jackson 序列化/反序列化 MQ 消息）
     *
     * @return Jackson2JsonMessageConverter 实例
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 配置 RabbitTemplate（设置连接工厂和 JSON 消息转换器）
     *
     * @param connectionFactory  RabbitMQ 连接工厂
     * @param jsonMessageConverter JSON 消息转换器
     * @return RabbitTemplate 实例
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }
}
