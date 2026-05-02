package org.woridge.ms_attendance_management.dto.tenant;

public class TenantContext {
    private static final ThreadLocal<String> currentTenant = new ThreadLocal<String>();

    public static void setCurrentTenant(String sub){
        TenantContext.currentTenant.set(sub);
    }

    public static String getCurrentTenant(){
        return TenantContext.currentTenant.get();
    }

    public static void clear(){
        TenantContext.currentTenant.remove();
    }
}
