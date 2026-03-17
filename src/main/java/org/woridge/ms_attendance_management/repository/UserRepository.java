package org.woridge.ms_attendance_management.repository;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.woridge.ms_attendance_management.model.User;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // Chercher un employé par son CIN
    // utilisé pendant l'enrollment pour vérifier qu'il existe
    Optional<User> findByCin(String cin);
}