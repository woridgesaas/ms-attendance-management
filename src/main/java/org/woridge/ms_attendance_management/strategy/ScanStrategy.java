package org.woridge.ms_attendance_management.strategy;

import org.woridge.ms_attendance_management.dto.rfid.ScanRequest;
import org.woridge.ms_attendance_management.dto.rfid.ScanResponse;

public interface ScanStrategy {

    ScanResponse handle(ScanRequest request);
}

