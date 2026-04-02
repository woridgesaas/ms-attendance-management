package org.woridge.ms_attendance_management.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.woridge.ms_attendance_management.dto.leave.LeaveRequestResponse;
import org.woridge.ms_attendance_management.service.LeaveRequestService;

@RestController
@RequestMapping("/leave-requests")
@RequiredArgsConstructor
@Validated
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<LeaveRequestResponse> createLeaveRequest(
            @RequestPart("data") String data,
            @RequestPart(value = "justification", required = false) MultipartFile justificationFile,
            @RequestHeader("X-User-CIN") String userCin) throws JsonProcessingException {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(leaveRequestService.createLeaveRequest(userCin, data, justificationFile));
    }
}