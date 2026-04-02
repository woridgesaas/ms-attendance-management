package org.woridge.ms_attendance_management.service;

import org.springframework.web.multipart.MultipartFile;
import org.woridge.ms_attendance_management.dto.leave.LeaveRequestFilter;
import org.woridge.ms_attendance_management.dto.leave.LeaveRequestListResponse;
import org.woridge.ms_attendance_management.dto.leave.LeaveRequestResponse;
import org.woridge.ms_attendance_management.enumeration.LeaveType;

import java.time.LocalDate;
import java.util.List;

public interface LeaveRequestService {
    LeaveRequestResponse createLeaveRequest(
            String userCin,
            String data,
            MultipartFile justificationFile
    );
    List<LeaveRequestListResponse> getAllLeaveRequests(
            String userCin,
            LeaveRequestFilter filter
    );
}
