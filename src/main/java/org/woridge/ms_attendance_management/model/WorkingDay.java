package org.woridge.ms_attendance_management.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "working_days")
@Getter
@Setter
@NoArgsConstructor
public class WorkingDay {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // La date du jour ex: 2026-03-12
    @Column(nullable = false)
    private LocalDate date;

    // Heure d'arrivée ex: 08:00
    // nullable = true car au CHECK_IN le check_out_time est encore null
    @Column(name = "check_in_time")
    private LocalTime checkInTime;

    // Heure de départ ex: 17:00
    // null tant que l'employé n'a pas encore pointé la sortie
    @Column(name = "check_out_time")
    private LocalTime checkOutTime;


    // L'employé qui a pointé — obligatoire
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by", nullable = false)
    private User performedBy;


    @CreationTimestamp
    @Column(name = "created_at", updatable = false , nullable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
    @OneToOne(mappedBy = "workingDay", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Overtime overtime;
}
