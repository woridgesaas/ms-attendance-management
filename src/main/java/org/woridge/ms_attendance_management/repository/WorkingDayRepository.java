package org.woridge.ms_attendance_management.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.woridge.ms_attendance_management.model.WorkingDay;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkingDayRepository extends JpaRepository<WorkingDay, UUID> {

    List<WorkingDay> findByPerformedBy_Id(UUID userId);

    @Query("""
        SELECT w 
        FROM WorkingDay w
        WHERE w.performedBy.id = :userId
        AND w.date BETWEEN :start AND :end
    """)
    List<WorkingDay> findBetweenDates(
            @Param("userId") UUID userId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    Optional<WorkingDay> findByPerformedBy_IdAndDate(UUID userId, LocalDate date);
}