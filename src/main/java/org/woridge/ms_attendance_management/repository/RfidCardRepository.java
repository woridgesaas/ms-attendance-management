package org.woridge.ms_attendance_management.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.woridge.ms_attendance_management.model.RfidCard;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RfidCardRepository extends JpaRepository<RfidCard, UUID> {

    @EntityGraph(attributePaths = {"employee"})
        // Chercher une carte par son UID physique — utilisé à chaque scan ESP8266
        // active = true : on ignore les cartes désactivées
    Optional<RfidCard> findByUidAndActiveTrue(String uid);

    // Vérifier si un employé a déjà une carte enregistrée
    // utilisé pendant l'enrollment pour éviter les doublons

    boolean existsByEmployeeId(UUID employeeId);

    //  vérifie uniquement les cartes actives
    // évite de bloquer le ré-enrollment après désactivation
    boolean existsByEmployeeIdAndActiveTrue(UUID employeeId);

    // récupère la carte active d'un employé
    // utilisé pour la désactivation de carte
    //  r.active (nom champ Java) et non r.isActive
    @Query("SELECT r FROM RfidCard r WHERE r.employee.id = :employeeId AND r.active = true")
    Optional<RfidCard> findActiveCardByEmployeeId(@Param("employeeId") UUID employeeId);

    Optional<RfidCard> findByUid(String uid);

}