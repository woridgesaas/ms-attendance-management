package org.woridge.ms_attendance_management.messaging.publisher;

import org.woridge.ms_attendance_management.messaging.event.OvertimeHandleEvent;

public interface OvertimeHandleEventPublisher {
    void publish(OvertimeHandleEvent event);
}
