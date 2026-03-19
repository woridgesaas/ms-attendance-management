package org.woridge.ms_attendance_management.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.woridge.ms_attendance_management.handler.AbsenceHandler;
import org.woridge.ms_attendance_management.handler.DefaultAbsenceHandler;
import org.woridge.ms_attendance_management.handler.JustificationAbsenceHandler;
import org.woridge.ms_attendance_management.handler.LeaveAbsenceHandler;

@Configuration
@RequiredArgsConstructor
public class BeanConfig {

    private final LeaveAbsenceHandler leaveHandler;
    private final JustificationAbsenceHandler justificationHandler;
    private final DefaultAbsenceHandler defaultHandler;

    @Bean
    public AbsenceHandler absenceHandlerChain() {
        leaveHandler.setNext(justificationHandler);
        justificationHandler.setNext(defaultHandler);
        return leaveHandler;  // on retourne le premier handler
    }
}
