package org.woridge.ms_attendance_management.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.woridge.ms_attendance_management.model.Justification;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JustificationRepository extends JpaRepository<Justification, UUID> {

// Un JUSTIFIED_JUSTIFICATION = justification existe + validation APPROVED
// MAIS pas de leave_day approuvé pour cette date
    @Query("""
        SELECT v.decision
        FROM Justification j
        JOIN j.leaveRequest lr
        JOIN lr.validations v
        WHERE lr.user.id = :userId
          AND :date BETWEEN lr.startDate AND lr.endDate
          AND NOT EXISTS (
              SELECT ld FROM LeaveDay ld
              WHERE ld.leaveRequest = lr
              AND ld.dateOfDay = :date
          )
        ORDER BY v.decisionDate DESC
        LIMIT 1
        """)
    Optional<String> findJustificationStatusForUserAndDate(
            @Param("userId") UUID userId,
            @Param("date") LocalDate date
    );
}
