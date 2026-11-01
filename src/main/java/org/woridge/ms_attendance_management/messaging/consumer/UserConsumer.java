package org.woridge.ms_attendance_management.messaging.consumer;

import java.util.UUID;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.woridge.ms_attendance_management.messaging.event.UserCreatedEvent;
import org.woridge.ms_attendance_management.model.User;
import org.woridge.ms_attendance_management.repository.UserRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserConsumer {

    private final UserRepository userRepository;
    @PostConstruct
    public void init() {
    }
    @RabbitListener(queues = "user.queue")
    public void consume(UserCreatedEvent event) {
        log.info("Receive user created event {}" , event.toString());
        try {

            if (userRepository.existsByCin(event.getCin())) {
                log.info("User already exists: {}", event.getCin());
                return;
            }
            UUID departmentId = null;

            if (event.getDepartmentId() != null) {
                departmentId = UUID.fromString(event.getDepartmentId());
            }

            User user = User.builder()

                    .cin(event.getCin())
                    .firstName(event.getFirstName())
                    .lastName(event.getLastName())
                    .email(event.getEmail())
                    .departmentId(departmentId)
                    .build();

            userRepository.save(user);


        } catch (Exception e) {
            log.error(" ERROR PROCESSING MESSAGE", e);
        }
    }
}