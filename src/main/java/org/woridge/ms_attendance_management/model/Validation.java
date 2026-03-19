package org.woridge.ms_attendance_management.model;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "validations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Validation {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "decision", length = 20)
    private String decision;

    @Column(name = "decision_date", nullable = false)
    private LocalDateTime decisionDate;

    @Column(name = "performed_by", nullable = false)
    private UUID performedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_request_id", nullable = false)
    private LeaveRequest leaveRequest;

    @PrePersist
    protected void onCreate() {
        if (this.decisionDate == null) {
            this.decisionDate = LocalDateTime.now();
        }
    }
}
