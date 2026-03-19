package org.woridge.ms_attendance_management.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.woridge.ms_attendance_management.enumeration.AbsenceType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "absences",
        uniqueConstraints = @UniqueConstraint(
                name = "unique_user_absence_date",
                columnNames = {"user_id", "absence_date"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Absence {

    @Id
    @UuidGenerator
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(name = "absence_date", nullable = false)
    private LocalDate absenceDate;
    @Enumerated(EnumType.STRING)
    @Column(name = "absence_type", nullable = false, length = 30)
    private AbsenceType absenceType;
    @Column(name = "is_final", nullable = false)
    @Builder.Default
    private boolean isFinal = false;
    @Column(name = "consecutive_days", nullable = false)
    @Builder.Default
    private int consecutiveDays = 0;
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}