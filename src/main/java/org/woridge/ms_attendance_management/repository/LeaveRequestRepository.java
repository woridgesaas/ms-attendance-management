package org.woridge.ms_attendance_management.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.woridge.ms_attendance_management.model.LeaveRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, UUID> {

    // Un JUSTIFIED_LEAVE = leave_day existe + validation APPROVED
    @Query("""
        SELECT COUNT(lr) > 0
        FROM LeaveRequest lr
        JOIN lr.leaveDays ld
        JOIN lr.validations v
        WHERE lr.user.id = :userId
          AND ld.dateOfDay = :date
          AND v.decision = 'APPROVED'
        """)
    boolean existsApprovedLeaveForUserAndDate(
            @Param("userId") UUID userId,
            @Param("date") LocalDate date
    );
    List<LeaveRequest> findByUserId(UUID userId);//next task

}