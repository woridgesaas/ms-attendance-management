package org.woridge.ms_attendance_management.messaging.publisher.implementation;

import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.woridge.ms_attendance_management.messaging.event.OvertimeHandleEvent;
import org.woridge.ms_attendance_management.messaging.publisher.OvertimeHandleEventPublisher;
import org.woridge.ms_attendance_management.properties.RabbitMQProperties;


@Component
@AllArgsConstructor
public class OvertimeHandleEventPublisherImpl implements OvertimeHandleEventPublisher {
    private RabbitTemplate rabbitTemplate;
    private RabbitMQProperties rabbitMQProperties;

    @Override
    public void publish(OvertimeHandleEvent event) {
        this.rabbitTemplate.convertAndSend(
                this.rabbitMQProperties.getExchange(),
                this.rabbitMQProperties.getRoutingKeys().getOvertimeProcessedAlert(),
                event
        );
    }
}
