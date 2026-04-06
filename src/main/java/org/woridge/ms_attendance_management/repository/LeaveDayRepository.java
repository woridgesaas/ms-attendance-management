package org.woridge.ms_attendance_management.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.woridge.ms_attendance_management.model.LeaveDay;

import java.util.UUID;

@Repository
public interface LeaveDayRepository extends CrudRepository<LeaveDay, UUID> {
}
