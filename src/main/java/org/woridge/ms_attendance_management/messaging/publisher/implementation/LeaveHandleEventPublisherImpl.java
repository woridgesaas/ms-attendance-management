package org.woridge.ms_attendance_management.messaging.publisher.implementation;

import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.woridge.ms_attendance_management.messaging.event.ApproveLeaveEvent;
import org.woridge.ms_attendance_management.messaging.publisher.LeaveHandleEventPublisher;
import org.woridge.ms_attendance_management.properties.RabbitMQProperties;

@Component
@AllArgsConstructor
public class LeaveHandleEventPublisherImpl implements LeaveHandleEventPublisher {

    private RabbitTemplate rabbitTemplate;
    private RabbitMQProperties rabbitMQProperties;

    @Override
    public void publish(ApproveLeaveEvent event) {
        System.out.println("Event [leave process] dispatched : " + event);
        this.rabbitTemplate.convertAndSend(
                this.rabbitMQProperties.getExchange(),
                this.rabbitMQProperties.getRoutingKeys().getLeaveProcessed(),
                event
        );
    }
}
