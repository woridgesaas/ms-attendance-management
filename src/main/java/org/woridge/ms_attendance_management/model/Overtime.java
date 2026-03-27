package org.woridge.ms_attendance_management.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.woridge.ms_attendance_management.enumeration.OvertimeStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name="overtimes")
public class Overtime {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "working_day_id", nullable = false)
    private WorkingDay workingDay;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "hours_25")
    private double hours25;

    @Column(name = "hours_50")
    private double hours50;

    @Column(name = "hours_100")
    private double hours100;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OvertimeStatus status = OvertimeStatus.PENDING;

    @Column(name = "manager_comment")
    private String managerComment;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public double getTotal(){
        return this.hours25 + this.hours50 + this.hours100;
    }
}

