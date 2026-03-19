package org.woridge.ms_attendance_management.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.woridge.ms_attendance_management.enumeration.AbsenceType;
import org.woridge.ms_attendance_management.model.Absence;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Repository
public interface AbsenceRepository extends JpaRepository<Absence, UUID> {

    // Cherche l'absence d'un user à une date précise
    Optional<Absence> findByUserIdAndAbsenceDate(UUID userId, LocalDate absenceDate);
    // Vérifie si une absence finale(cad wsslat alert critique) existe pour ce user à cette date
    boolean existsByUserIdAndAbsenceDateAndIsFinalTrue(UUID userId, LocalDate absenceDate);
    // Récupère toutes les absences d'un user triées par date décroissante
    List<Absence> findByUserIdOrderByAbsenceDateDesc(UUID userId);
}
