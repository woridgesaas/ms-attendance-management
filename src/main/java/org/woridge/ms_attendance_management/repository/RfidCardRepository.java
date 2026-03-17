package org.woridge.ms_attendance_management.repository;


import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
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
}