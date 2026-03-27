package org.woridge.ms_attendance_management.messaging.consumer;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.woridge.ms_attendance_management.messaging.event.HolidayCreatedEvent;
import org.woridge.ms_attendance_management.model.Holiday;
import org.woridge.ms_attendance_management.repository.HolidayRepository;

@Component
@RequiredArgsConstructor
public class HolidayConsumer {

    private final HolidayRepository holidayRepository;

    @RabbitListener(queues = "${rabbitmq.holiday-queue}")
    public void consume(HolidayCreatedEvent event) {

        if (holidayRepository.existsByDate(event.getDate())) {
            return;
        }

        Holiday holiday = Holiday.builder()
                .date(event.getDate())
                .build();

        holidayRepository.save(holiday);
    }
}