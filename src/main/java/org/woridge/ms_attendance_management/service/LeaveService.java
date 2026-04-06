package org.woridge.ms_attendance_management.service;

import org.woridge.ms_attendance_management.model.LeaveRequest;

import java.util.UUID;

public interface LeaveService {
     LeaveRequest approveLeave(UUID leaveRequestId , String managerCin);
}
