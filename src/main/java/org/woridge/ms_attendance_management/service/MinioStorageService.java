package org.woridge.ms_attendance_management.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface MinioStorageService {
    String uploadJustification(MultipartFile file, UUID leaveRequestId);
}
