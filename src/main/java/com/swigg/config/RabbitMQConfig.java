package com.swigg.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.rider.notification.exchange:rider.notification.exchange}")
    private String riderNotificationExchange;

    @Value("${rabbitmq.rider.notification.queue:rider.notification.queue}")
    private String riderNotificationQueue;

    @Value("${rabbitmq.location.tracking.exchange:location.tracking.exchange}")
    private String locationTrackingExchange;

    @Value("${rabbitmq.location.tracking.queue:location.tracking.queue}")
    private String locationTrackingQueue;

    @Value("${rabbitmq.order.status.exchange:order.status.exchange}")
    private String orderStatusExchange;

    @Value("${rabbitmq.order.status.queue:order.status.queue}")
    private String orderStatusQueue;

    @Bean
    public MessageConverter converter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate template(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter());
        return rabbitTemplate;
    }

    // Rider Notification Fanout Exchange
    @Bean
    public FanoutExchange riderNotificationFanoutExchange() {
        return new FanoutExchange(riderNotificationExchange);
    }

    @Bean
    public Queue riderNotificationQueue() {
        return new Queue(riderNotificationQueue, true);
    }

    @Bean
    public Binding riderNotificationBinding() {
        return BindingBuilder
                .bind(riderNotificationQueue())
                .to(riderNotificationFanoutExchange());
    }

    // Location Tracking Fanout Exchange
    @Bean
    public FanoutExchange locationTrackingFanoutExchange() {
        return new FanoutExchange(locationTrackingExchange);
    }

    @Bean
    public Queue locationTrackingQueue() {
        return new Queue(locationTrackingQueue, true);
    }

    @Bean
    public Binding locationTrackingBinding() {
        return BindingBuilder
                .bind(locationTrackingQueue())
                .to(locationTrackingFanoutExchange());
    }

    // Order Status Fanout Exchange
    @Bean
    public FanoutExchange orderStatusFanoutExchange() {
        return new FanoutExchange(orderStatusExchange);
    }

    @Bean
    public Queue orderStatusQueue() {
        return new Queue(orderStatusQueue, true);
    }

    @Bean
    public Binding orderStatusBinding() {
        return BindingBuilder
                .bind(orderStatusQueue())
                .to(orderStatusFanoutExchange());
    }
}
