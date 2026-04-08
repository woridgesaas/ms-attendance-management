package org.woridge.ms_attendance_management.config;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.woridge.ms_attendance_management.properties.RabbitMQProperties;

@Configuration
@RequiredArgsConstructor
public class RabbitMQConfig {

    private final RabbitMQProperties properties;

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(properties.getExchange());
    }

    @Bean
    public Queue holidayQueue() {
        return new Queue(properties.getHolidayQueue());
    }

    @Bean
    public Binding holidayBinding() {
        return BindingBuilder
                .bind(holidayQueue())
                .to(exchange())
                .with(properties.getRoutingKeys().getHolidayCreated());
    }

    @Bean
    public Queue leaveProcessedQueue(){
        return QueueBuilder
                .durable("notification.leave.processed.approve.queue").
                build();
    }

    @Bean
    public TopicExchange leaveProcessedExchange(){
        return ExchangeBuilder
                .topicExchange(this.properties.getExchange())
                .durable(true)
                .build();
    }
    @Bean
    public Binding LeaveProcessedBinding(){
        return BindingBuilder
                .bind(leaveProcessedQueue())
                .to(exchange())
                .with(this.properties.getRoutingKeys().getLeaveProcessed());

    }
}