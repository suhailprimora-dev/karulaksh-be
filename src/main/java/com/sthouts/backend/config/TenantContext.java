package com.sthouts.backend.config;

public class TenantContext {
    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    public static void setTenantEmail(String tenantEmail) {
        CURRENT_TENANT.set(tenantEmail);
    }

    public static String getTenantEmail() {
        return CURRENT_TENANT.get();
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
