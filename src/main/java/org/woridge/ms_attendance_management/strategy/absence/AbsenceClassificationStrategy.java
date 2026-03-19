package org.woridge.ms_attendance_management.strategy.absence;

import org.woridge.ms_attendance_management.enumeration.AbsenceType;

import java.time.LocalDate;
import java.util.UUID;

public interface AbsenceClassificationStrategy {

    /**
     * Vérifie si cette stratégie peut traiter le cas
     */
    boolean canHandle(UUID userId, LocalDate date);

    /**
     * Classifie l'absence et retourne le type
     */
    AbsenceType classify(UUID userId, LocalDate date);
}
