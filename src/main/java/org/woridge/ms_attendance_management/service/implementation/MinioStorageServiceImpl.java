package org.woridge.ms_attendance_management.service.implementation;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.woridge.ms_attendance_management.service.MinioStorageService;
import org.woridge.ms_attendance_management.utils.FileUtils;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MinioStorageServiceImpl implements MinioStorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    public String uploadJustification(MultipartFile file, UUID leaveRequestId) {
        FileUtils.validateJustificationFile(file);
        try {
            String objectName = "justifications/" + leaveRequestId + "/"
                    + UUID.randomUUID() + "_" + file.getOriginalFilename();

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            return objectName;

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du stockage du justificatif.", e);
        }
    }
}
