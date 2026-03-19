package org.woridge.ms_attendance_management.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.woridge.ms_attendance_management.enumeration.AlertLevel;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "absence_alerts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AbsenceAlert {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "alert_level", nullable = false, length = 10)
    private AlertLevel alertLevel;

    @Column(name = "triggered_at", nullable = false)
    private LocalDateTime triggeredAt;
    @Column(name = "consecutive_days", nullable = false)
    private int consecutiveDays;

    @PrePersist
    protected void onCreate() {
        if (this.triggeredAt == null) {
            this.triggeredAt = LocalDateTime.now();
        }
    }
}