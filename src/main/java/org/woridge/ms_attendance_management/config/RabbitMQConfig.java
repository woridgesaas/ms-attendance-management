package org.woridge.ms_attendance_management.config;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
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
}