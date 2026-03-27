package org.woridge.ms_attendance_management.service;

import org.woridge.ms_attendance_management.model.Overtime;

import java.util.List;
import java.util.UUID;

public interface OvertimeService {
    public List<Overtime> getPendingByDepartment(String managerCin);
    public void approveOvertime(UUID overtimeId , String approvedBy);
    public void refuseOvertime(UUID overtimeId , String approvedBy , String raison);
}
