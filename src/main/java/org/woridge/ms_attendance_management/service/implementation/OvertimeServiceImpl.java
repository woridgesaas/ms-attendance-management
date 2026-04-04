package org.woridge.ms_attendance_management.service.implementation;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.woridge.ms_attendance_management.enumeration.OvertimeStatus;
import org.woridge.ms_attendance_management.exception.overtime.OvertimeAlreadyProcessedException;
import org.woridge.ms_attendance_management.exception.overtime.OvertimeNotFoundException;
import org.woridge.ms_attendance_management.exception.user.UserNotFoundException;
import org.woridge.ms_attendance_management.mapper.OvertimeMapper;
import org.woridge.ms_attendance_management.messaging.event.OvertimeHandleEvent;
import org.woridge.ms_attendance_management.messaging.publisher.OvertimeHandleEventPublisher;
import org.woridge.ms_attendance_management.messaging.publisher.implementation.OvertimeHandleEventPublisherImpl;
import org.woridge.ms_attendance_management.model.Overtime;
import org.woridge.ms_attendance_management.model.User;
import org.woridge.ms_attendance_management.repository.OvertimeRepository;
import org.woridge.ms_attendance_management.repository.UserRepository;
import org.woridge.ms_attendance_management.service.OvertimeService;

import java.util.List;
import java.util.UUID;
@Service
@Transactional
@AllArgsConstructor
public class OvertimeServiceImpl implements OvertimeService {

    @Autowired
    private OvertimeRepository overtimeRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OvertimeHandleEventPublisher overtimeHandleEventPublisher;

    @Override
    public List<Overtime> getPendingByDepartment(String cin) {
        User manager = this.userRepository.findByCin(cin).orElseThrow(()-> new UserNotFoundException("user nont found with CIN :" + cin ));
        UUID departId = manager.getDepartmentId();
        List<Overtime> overtimes = overtimeRepository
                .findAllByDepartment(departId);

        return overtimes;
    }

    @Override
    public void approveOvertime(UUID overtimeId, String managerCin) {
        User manager = userRepository.findByCin(managerCin)
                .orElseThrow(() -> new UserNotFoundException("Manager not found with  " + managerCin));
        Overtime ot = findAndValidatePending(overtimeId);

        ot.setStatus(OvertimeStatus.APPROVED);
        ot.setApprovedBy(manager.getId());

        // --Publish event
        var employee = ot.getUser();
        OvertimeHandleEvent event = OvertimeMapper.toOvertimeAlertEvent(
                ot,
                employee.getCin() ,
                employee.getEmail()
        );
        System.out.println("Event has been dispatched :)" + event.toString()) ;
        this.overtimeHandleEventPublisher.publish(event);
        overtimeRepository.save(ot);
    }

    @Override
    public void refuseOvertime(UUID overtimeId, String managerCin, String raison) {
        User manager = userRepository.findByCin(managerCin)
                .orElseThrow(() -> new UserNotFoundException("Manager introuvable with : " + managerCin));

        Overtime ot = findAndValidatePending(overtimeId);

        ot.setStatus(OvertimeStatus.REJECTED);
        ot.setApprovedBy(manager.getId());
        ot.setManagerComment(raison);

        // --Publish event
        var employee = ot.getUser();
        OvertimeHandleEvent event = OvertimeMapper.toOvertimeAlertEvent(
                ot,
                employee.getCin() ,
                employee.getEmail()
        );
        System.out.println("Evenet has been dispatched : " + event.toString());
        this.overtimeHandleEventPublisher.publish(event);

        overtimeRepository.save(ot);
    }

    private Overtime findAndValidatePending(UUID id) {
        Overtime ot = overtimeRepository.findById(id)
                .orElseThrow(() -> new OvertimeNotFoundException("Overtime not found with this id" +id));

        if (ot.getStatus() != OvertimeStatus.PENDING) {
            throw new OvertimeAlreadyProcessedException("This demande is already processed");
        }
        return ot;
    }
}
