package com.example.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Chang Qi
 * @date 2022/4/28 16:22
 * @description
 * @Version V1.0
 */

@Configuration
public class RabbitMqConfig {

    //队列名称
    public final static String ES_QUEUE = "es_queue";
    public final static String ES_EXCHANGE = "es_change";
    public final static String ES_BIND_KEY = "ex_index_message";

    @Bean
    public Queue exQueue() {
        return new Queue(ES_QUEUE);
    }

    @Bean
    DirectExchange exchange() {
        return new DirectExchange(ES_EXCHANGE);
    }

    @Bean
    Binding binding(Queue exQueue, DirectExchange exchange) {
        return BindingBuilder.bind(exQueue).to(exchange).with(ES_BIND_KEY);
    }

}
