package org.woridge.ms_attendance_management.messaging.publisher.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.woridge.ms_attendance_management.enumeration.AlertLevel;
import org.woridge.ms_attendance_management.messaging.event.AbsenceAlertEvent;
import org.woridge.ms_attendance_management.messaging.publisher.AbsenceAlertPublisher;
import org.woridge.ms_attendance_management.properties.RabbitMQProperties;

@Component
@RequiredArgsConstructor
public class AbsenceAlertPublisherImpl implements AbsenceAlertPublisher {

    private final RabbitTemplate     rabbitTemplate;
    private final RabbitMQProperties rabbitMQProperties;

    @Override
    public void publish(AbsenceAlertEvent event) {
        String routingKey = event.alertLevel() == AlertLevel.RH
                ? rabbitMQProperties.getRoutingKeys().getAbsenceAlertRh()
                : rabbitMQProperties.getRoutingKeys().getAbsenceAlertCeo();

        rabbitTemplate.convertAndSend(
                rabbitMQProperties.getExchange(),
                routingKey,
                event
        );
    }
}