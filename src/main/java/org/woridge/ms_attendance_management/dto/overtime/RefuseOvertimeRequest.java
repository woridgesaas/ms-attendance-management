package org.woridge.ms_attendance_management.dto.overtime;

import java.util.UUID;

public record RefuseOvertimeRequest(UUID overtimeId , String raison) {
}
