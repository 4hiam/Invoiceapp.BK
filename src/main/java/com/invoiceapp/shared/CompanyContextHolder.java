package com.invoiceapp.shared;

import java.util.UUID;

/**
 * Thread-local holder for the current company context.
 * Set by CompanyContextFilter on every request that includes a company ID.
 * Used by services to enforce multi-company data isolation.
 */
public class CompanyContextHolder {

    private static final ThreadLocal<UUID> CURRENT_COMPANY = new ThreadLocal<>();

    public static void setCompanyId(UUID companyId) {
        CURRENT_COMPANY.set(companyId);
    }

    public static UUID getCompanyId() {
        return CURRENT_COMPANY.get();
    }

    public static UUID requireCompanyId() {
        UUID companyId = CURRENT_COMPANY.get();
        if (companyId == null) {
            throw new IllegalStateException("No company context set for this request");
        }
        return companyId;
    }

    public static void clear() {
        CURRENT_COMPANY.remove();
    }
}
