package org.woridge.ms_attendance_management.config;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.woridge.ms_attendance_management.properties.RabbitMQProperties;

@Configuration
@RequiredArgsConstructor
public class RabbitMQConfig {

    private final RabbitMQProperties properties;
    @Bean
    public Queue userQueue() {
        return new Queue("user.queue");
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        return factory;
    }
    @Bean
    public Queue holidayQueue() {
        return new Queue(properties.getHolidayQueue());
    }

    @Bean
    public Binding holidayBinding(TopicExchange notificationExchange) {
        return BindingBuilder
                .bind(holidayQueue())
                .to(notificationExchange)
                .with(properties.getRoutingKeys().getHolidayCreated());
    }
    @Bean
    public Binding userBinding(TopicExchange notificationExchange) {
        return BindingBuilder
                .bind(userQueue())
                .to(notificationExchange)
                .with("user.created");
    }
}