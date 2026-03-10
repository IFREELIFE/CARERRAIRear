package com.endcareerai.platform.config;

import com.endcareerai.platform.common.Constants;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public DirectExchange llmExchange() {
        return new DirectExchange(Constants.MQ_EXCHANGE_LLM);
    }

    @Bean
    public Queue llmTaskQueue() {
        return QueueBuilder.durable(Constants.MQ_QUEUE_LLM_TASK).build();
    }

    @Bean
    public Binding llmBinding(Queue llmTaskQueue, DirectExchange llmExchange) {
        return BindingBuilder.bind(llmTaskQueue).to(llmExchange).with(Constants.MQ_ROUTING_KEY_LLM);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }
}
