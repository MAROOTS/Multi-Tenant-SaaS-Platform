package com.maroots.backend.entity;

public class TenantContextHolder {

    private static final ThreadLocal<String> context = new ThreadLocal<>();

    public static void setTenantId(String tenantId) {
        context.set(tenantId);
    }

    public static String getTenantId() {
        return context.get();
    }

    public static void clear() {
        context.remove();
    }
}
