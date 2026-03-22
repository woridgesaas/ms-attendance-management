package org.woridge.ms_attendance_management.config;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.woridge.ms_attendance_management.handler.AbsenceHandler;
import org.woridge.ms_attendance_management.handler.DefaultAbsenceHandler;
import org.woridge.ms_attendance_management.handler.JustificationAbsenceHandler;
import org.woridge.ms_attendance_management.handler.LeaveAbsenceHandler;
import org.woridge.ms_attendance_management.properties.RabbitMQProperties;


@Configuration
@RequiredArgsConstructor
public class BeanConfig {

    private final LeaveAbsenceHandler leaveHandler;
    private final JustificationAbsenceHandler justificationHandler;
    private final DefaultAbsenceHandler defaultHandler;
    private final RabbitMQProperties rabbitMQProperties;


    @Bean
    public AbsenceHandler absenceHandlerChain() {
        leaveHandler.setNext(justificationHandler);
        justificationHandler.setNext(defaultHandler);
        return leaveHandler;  // on retourne le premier handler
    }
    @Bean
    public TopicExchange notificationExchange() {
        return ExchangeBuilder
                .topicExchange(rabbitMQProperties.getExchange())
                .durable(true)
                .build();
    }
    @Bean
    public Queue absenceAlertRhQueue() {
        return QueueBuilder
                .durable("notification.absence.alert.rh.queue")
                .build();
    }
    @Bean
    public Queue absenceAlertCeoQueue() {
        return QueueBuilder
                .durable("notification.absence.alert.ceo.queue")
                .build();
    }
    @Bean
    public Binding absenceAlertRhBinding() {
        return BindingBuilder
                .bind(absenceAlertRhQueue())
                .to(notificationExchange())
                .with(rabbitMQProperties.getRoutingKeys().getAbsenceAlertRh());
    }
    @Bean
    public Binding absenceAlertCeoBinding() {
        return BindingBuilder
                .bind(absenceAlertCeoQueue())
                .to(notificationExchange())
                .with(rabbitMQProperties.getRoutingKeys().getAbsenceAlertCeo());
    }
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
