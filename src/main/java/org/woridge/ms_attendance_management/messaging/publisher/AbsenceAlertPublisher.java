package org.woridge.ms_attendance_management.messaging.publisher;

import org.woridge.ms_attendance_management.messaging.event.AbsenceAlertEvent;

public interface AbsenceAlertPublisher {
    void publish(AbsenceAlertEvent event);
}