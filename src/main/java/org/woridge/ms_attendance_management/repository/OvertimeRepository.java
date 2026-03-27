package org.woridge.ms_attendance_management.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.woridge.ms_attendance_management.enumeration.OvertimeStatus;
import org.woridge.ms_attendance_management.model.Overtime;

import java.util.List;
import java.util.UUID;

@Repository
public interface OvertimeRepository extends JpaRepository<Overtime , UUID> {
    //Get all overtimes performed by employes in the department of manager
    @Query("SELECT o FROM Overtime o JOIN o.workingDay wd JOIN wd.performedBy u " +
            "WHERE u.departmentId = :deptId")
    List<Overtime> findAllByDepartment(UUID deptId);
}
