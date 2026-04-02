package org.woridge.ms_attendance_management.service;

import org.springframework.web.multipart.MultipartFile;
import org.woridge.ms_attendance_management.dto.leave.LeaveRequestResponse;

public interface LeaveRequestService {
    LeaveRequestResponse createLeaveRequest(
            String userCin,
            String data,
            MultipartFile justificationFile
    );
}
