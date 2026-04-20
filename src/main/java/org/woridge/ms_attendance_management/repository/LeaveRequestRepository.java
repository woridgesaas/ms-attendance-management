package org.woridge.ms_attendance_management.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.woridge.ms_attendance_management.enumeration.LeaveType;
import org.woridge.ms_attendance_management.model.LeaveRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, UUID> {

    // Un JUSTIFIED_LEAVE = leave_day existe + validation APPROVED
    @Query("""
        SELECT COUNT(lr) > 0
        FROM LeaveRequest lr
        JOIN lr.leaveDays ld
        JOIN lr.validations v
        WHERE lr.user.id = :userId
          AND ld.dateOfDay = :date
          AND v.decision = 'APPROVED'
        """)
    boolean existsApprovedLeaveForUserAndDate(
            @Param("userId") UUID userId,
            @Param("date") LocalDate date
    );

    @Query("""
    SELECT lr FROM LeaveRequest lr
    LEFT JOIN FETCH lr.validation v
    LEFT JOIN FETCH v.performedBy
    WHERE lr.user.id = :userId
    AND (:leaveType IS NULL OR lr.leaveType = :leaveType)
    AND (
        :status IS NULL
        OR (:status = 'NEW' AND v IS NULL)
        OR (:status != 'NEW' AND v IS NOT NULL AND CAST(v.decision AS string) = :status)
    )
    AND (:startDate IS NULL OR lr.startDate >= :startDate)
    AND (:endDate IS NULL OR lr.endDate <= :endDate)
    ORDER BY lr.requestDate DESC
""")
    List<LeaveRequest> findAllByFilters(
            @Param("userId")    UUID userId,
            @Param("leaveType") LeaveType leaveType,
            @Param("status")    String status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate")   LocalDate endDate
    );

    @Query("""
        SELECT lr FROM LeaveRequest lr
        JOIN FETCH lr.user u
        LEFT JOIN FETCH lr.validation v
        WHERE u.departmentId = :departmentId
        ORDER BY lr.requestDate DESC
    """)
    List<LeaveRequest> findPendingRequestsByDepartment(
            @Param("departmentId") UUID departmentId
    );
    @Query("""
    SELECT COUNT(l) > 0 FROM LeaveRequest l
    WHERE l.user.id = :userId
    AND l.startDate <= :endDate
    AND l.endDate >= :startDate
""")
    boolean existsOverlap(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    //  RH voit toutes les demandes sans validation (PENDING)
    @Query("""
        SELECT lr FROM LeaveRequest lr
        JOIN FETCH lr.user u
        LEFT JOIN FETCH lr.validation v
        ORDER BY lr.requestDate DESC
    """)
    List<LeaveRequest> findAllPendingRequests();

    // Congés APPROUVÉS d'un département pour un mois donné (pour le calendrier Manager)
    @Query("""
    SELECT lr FROM LeaveRequest lr
    JOIN FETCH lr.user u
    JOIN FETCH lr.validation v
    WHERE u.departmentId = :departmentId
      AND v.decision = 'APPROVED'
      AND lr.startDate <= :endOfMonth
      AND lr.endDate >= :startOfMonth
    ORDER BY lr.startDate ASC
""")
    List<LeaveRequest> findApprovedLeavesByDepartmentAndMonth(
            @Param("departmentId") UUID departmentId,
            @Param("startOfMonth") LocalDate startOfMonth,
            @Param("endOfMonth") LocalDate endOfMonth
    );

    // Congés APPROUVÉS de TOUS les départements pour un mois (pour le calendrier RH)
    @Query("""
    SELECT lr FROM LeaveRequest lr
    JOIN FETCH lr.user u
    JOIN FETCH lr.validation v
    WHERE v.decision = 'APPROVED'
      AND lr.startDate <= :endOfMonth
      AND lr.endDate >= :startOfMonth
    ORDER BY u.departmentId ASC, lr.startDate ASC
""")
    List<LeaveRequest> findAllApprovedLeavesByMonth(
            @Param("startOfMonth") LocalDate startOfMonth,
            @Param("endOfMonth") LocalDate endOfMonth
    );

    //  Liste des department IDs distincts ayant des congés approuvés
    @Query("""
    SELECT DISTINCT u.departmentId FROM LeaveRequest lr
    JOIN lr.user u
    JOIN lr.validation v
    WHERE v.decision = 'APPROVED'
""")
    List<UUID> findDistinctDepartmentIdsWithApprovedLeaves();
}