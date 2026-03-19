package org.woridge.ms_attendance_management.handler;

import org.woridge.ms_attendance_management.enumeration.AbsenceType;

import java.time.LocalDate;
import java.util.UUID;

public abstract class AbsenceHandler {

    protected AbsenceHandler next;

    // Assemble la chaîne : leaveHandler.setNext(justifHandler).setNext(defaultHandler)
    public AbsenceHandler setNext(AbsenceHandler next) {
        this.next = next;
        return next;
    }

    // Chaque handler implémente sa propre logique
    public abstract AbsenceType handle(UUID userId, LocalDate date);

    // Si ce handler ne peut pas traiter → passe au suivant
    protected AbsenceType passToNext(UUID userId, LocalDate date) {
        if (next != null) {
            return next.handle(userId, date);
        }
        return AbsenceType.UNJUSTIFIED;
    }
}
