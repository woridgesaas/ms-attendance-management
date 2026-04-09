package org.woridge.ms_attendance_management.service;

import org.woridge.ms_attendance_management.dto.leave.LeaveRequestAdminResponse;
import org.woridge.ms_attendance_management.dto.leave.LeaveRequestListResponse;
import org.woridge.ms_attendance_management.model.LeaveRequest;

import java.util.List;
import java.util.UUID;

public interface LeaveService {
     LeaveRequest approveLeave(UUID leaveRequestId , String managerCin);
    List<LeaveRequestAdminResponse> getPendingLeaves(String managerCin, String role);
    LeaveRequest rejectLeave(UUID leaveRequestId, String managerCin);

}

