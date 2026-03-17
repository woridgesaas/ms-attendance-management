package org.woridge.ms_attendance_management.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "rfid_cards")
@Getter
@Setter
@NoArgsConstructor
public class RfidCard {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // L'UID physique de la carte lu par l'ESP8266 ex: "A3:F2:11:BC"
    @Column(nullable = false, unique = true)
    private String uid;

    // Relation : plusieurs cartes peuvent appartenir à un employé
    // mais une carte appartient à UN SEUL employé
    @ManyToOne(fetch = FetchType.LAZY)   // LAZY = ne charge pas l'employé
    @JoinColumn(                          // automatiquement, seulement si besoin
            name = "employee_id",             // nom de la colonne FK en DB
            nullable = false
    )
    private User employee;

    @Column(name = "enrolled_at", updatable = false, nullable = false)
    private OffsetDateTime enrolledAt = OffsetDateTime.now();

    // false = carte désactivée (perdue, volée...)
    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}