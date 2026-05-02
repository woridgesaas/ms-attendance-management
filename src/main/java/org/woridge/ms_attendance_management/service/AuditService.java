package org.woridge.ms_attendance_management.service;

public interface AuditService {
    void log(String action, String target, String status);
}
