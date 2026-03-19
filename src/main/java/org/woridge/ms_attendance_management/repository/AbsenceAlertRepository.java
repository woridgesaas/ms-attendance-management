package org.woridge.ms_attendance_management.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.woridge.ms_attendance_management.enumeration.AlertLevel;
import org.woridge.ms_attendance_management.model.AbsenceAlert;

import java.time.LocalDateTime;
import java.util.UUID;
@Repository
public interface AbsenceAlertRepository extends JpaRepository<AbsenceAlert, UUID> {

    // Vérifie si une alerte du même niveau a déjà été déclenchée aujourd'hui
    // pour éviter les doublons
    boolean existsByUserIdAndAlertLevelAndTriggeredAtBetween(
            UUID userId,
            AlertLevel alertLevel,
            LocalDateTime from,
            LocalDateTime to
    );
}
