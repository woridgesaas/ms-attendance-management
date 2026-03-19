package org.woridge.ms_attendance_management.model;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "justifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Justification {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "submission_date")
    private LocalDateTime submissionDate;

    @Column(name = "path", nullable = false)
    private String path;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_request_id", nullable = false)
    private LeaveRequest leaveRequest;

    @PrePersist
    protected void onCreate() {
        if (this.submissionDate == null) {
            this.submissionDate = LocalDateTime.now();
        }
    }
}
