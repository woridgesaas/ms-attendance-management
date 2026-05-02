package org.woridge.ms_attendance_management.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TenantReadyEvent {
    private int subscriptionId;
    private String subdomain;
    private String serviceKey;
}
