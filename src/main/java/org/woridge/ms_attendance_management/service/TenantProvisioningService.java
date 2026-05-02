package org.woridge.ms_attendance_management.service;

public interface TenantProvisioningService {
    void runMigrationsForTenant(String subdomain, String serviceKey);
}
