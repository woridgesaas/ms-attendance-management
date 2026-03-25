package org.woridge.ms_attendance_management.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "rabbitmq")
@Getter
@Setter
public class RabbitMQProperties {

    private String exchange;
    private RabbitMQRoutingKeysProperties routingKeys;
    private String holidayQueue;
}