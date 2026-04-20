package org.woridge.ms_attendance_management.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.woridge.ms_attendance_management.service.BalanceService;
@RestController
@AllArgsConstructor
@RequestMapping("/balances")
public class BalanceController {
    private final BalanceService balanceService;
    @GetMapping
    public ResponseEntity<Integer> getRemainingBalance(
            @RequestHeader("X-User-CIN") String userCin) {
        return ResponseEntity.ok(balanceService.getRemainingBalance(userCin));
    }
}
