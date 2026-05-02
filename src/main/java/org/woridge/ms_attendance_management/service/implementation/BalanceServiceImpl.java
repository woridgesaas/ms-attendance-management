package org.woridge.ms_attendance_management.service.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.woridge.ms_attendance_management.exception.user.UserNotFoundException;
import org.woridge.ms_attendance_management.model.Balance;
import org.woridge.ms_attendance_management.model.User;
import org.woridge.ms_attendance_management.repository.BalanceRepository;
import org.woridge.ms_attendance_management.repository.UserRepository;
import org.woridge.ms_attendance_management.service.BalanceService;
@Service
@RequiredArgsConstructor
public class BalanceServiceImpl implements BalanceService {
    private final BalanceRepository balanceRepository;
    private final UserRepository userRepository;
    public Integer getRemainingBalance(String userCin) {
        User user = userRepository.findByCin(userCin)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return balanceRepository
                .findByUserId(user.getId())
                .map(Balance::getRemainingBalance)
                .orElse(0);
    }
}
