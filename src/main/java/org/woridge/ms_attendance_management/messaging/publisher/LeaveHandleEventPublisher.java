package org.woridge.ms_attendance_management.messaging.publisher;

import org.woridge.ms_attendance_management.messaging.event.ApproveLeaveEvent;

public interface LeaveHandleEventPublisher {
    void publish(ApproveLeaveEvent event);
}
