package org.woridge.ms_attendance_management.utils;

import org.springframework.web.multipart.MultipartFile;
import org.woridge.ms_attendance_management.exception.leave.LeaveRequestException;

import java.util.Arrays;
import java.util.List;

public final class FileUtils {

    private FileUtils() {
        // Utility class — pas d'instanciation
    }

    private static final long MAX_SIZE = 5 * 1024 * 1024; // 5MB

    private static final List<String> ALLOWED_TYPES = Arrays.asList(
            "application/pdf",
            "image/jpeg",
            "image/png"
    );

    public static void validateJustificationFile(MultipartFile file) {
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new LeaveRequestException(
                    "Type de fichier non supporté. Formats acceptés : PDF, JPEG, PNG."
            );
        }
        if (file.getSize() > MAX_SIZE) {
            throw new LeaveRequestException(
                    "Fichier trop volumineux. Taille maximale : 5MB."
            );
        }
    }
}
