package com.invoiceapp.recurring;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RecurringInvoiceRepository extends JpaRepository<RecurringInvoice, UUID> {

    Page<RecurringInvoice> findByCompanyIdAndIsActiveTrue(UUID companyId, Pageable pageable);

    Optional<RecurringInvoice> findByIdAndCompanyId(UUID id, UUID companyId);

    @Query("SELECT r FROM RecurringInvoice r WHERE r.isActive = true " +
           "AND r.nextGenerationDate <= :now " +
           "AND r.lastNotificationStatus = 'pending'")
    List<RecurringInvoice> findDueForNotification(@Param("now") Instant now);

    @Query("SELECT r FROM RecurringInvoice r WHERE r.isActive = true " +
           "AND r.lastNotificationStatus = 'sent' " +
           "AND r.nextGenerationDate <= :expiredBefore")
    List<RecurringInvoice> findExpiredNotifications(@Param("expiredBefore") Instant expiredBefore);
}
