package org.woridge.ms_attendance_management.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.woridge.ms_attendance_management.model.WorkingDay;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkingDayRepository extends JpaRepository<WorkingDay, UUID> {

    @Query("SELECT w FROM WorkingDay w WHERE w.performedBy.id = :employeeId AND w.date = :date ORDER BY w.createdAt DESC LIMIT 1")
    Optional<WorkingDay> findByPerformedByIdAndDate(
            @Param("employeeId") UUID employeeId,
            @Param("date") LocalDate date
    );
}
