package org.woridge.ms_attendance_management.strategy.rfid.implementation;



import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.woridge.ms_attendance_management.dto.rfid.ScanRequest;
import org.woridge.ms_attendance_management.dto.rfid.ScanResponse;
import org.woridge.ms_attendance_management.model.RfidCard;
import org.woridge.ms_attendance_management.model.User;
import org.woridge.ms_attendance_management.exception.rfid.EmployeeAlreadyHasCardException;
import org.woridge.ms_attendance_management.exception.rfid.RfidCardAlreadyExistsException;
import org.woridge.ms_attendance_management.mapper.RfidCardMapper;
import org.woridge.ms_attendance_management.repository.RfidCardRepository;
import org.woridge.ms_attendance_management.repository.UserRepository;
import org.woridge.ms_attendance_management.strategy.rfid.ScanStrategy;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EnrollmentStrategyImpl implements ScanStrategy {

    private final RfidCardRepository rfidCardRepository;
    private final UserRepository userRepository;
    private final RfidCardMapper rfidCardMapper;

    // L'employeeId est défini par le Resolver avant chaque enrollment
    @Setter
    private UUID pendingEmployeeId;

    @Override
    public ScanResponse handle(ScanRequest request) {

        // 1. Carte déjà enregistrée ?
        if (rfidCardRepository.findByUidAndActiveTrue(request.getUid()).isPresent()) {
            throw new RfidCardAlreadyExistsException(request.getUid());
        }

        // 2. Employé existe ?
        User employee = userRepository.findById(pendingEmployeeId)
                .orElseThrow(() -> new RuntimeException(
                        "Employé introuvable : " + pendingEmployeeId
                ));

        // 3. Employé a déjà une carte ?
        if (rfidCardRepository.existsByEmployeeId(pendingEmployeeId)) {
            throw new EmployeeAlreadyHasCardException(pendingEmployeeId);
        }

        // 4. Associer la carte à l'employé
        RfidCard card = new RfidCard();
        card.setUid(request.getUid());
        card.setEmployee(employee);
        rfidCardRepository.save(card);

        return rfidCardMapper.toEnrolledResponse(card);
    }
}