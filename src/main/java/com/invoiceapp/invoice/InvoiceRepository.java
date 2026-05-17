package com.invoiceapp.invoice;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    Page<Invoice> findByCompanyId(UUID companyId, Pageable pageable);

    Page<Invoice> findByCompanyIdAndStatus(UUID companyId, String status, Pageable pageable);

    Page<Invoice> findByCompanyIdAndClientId(UUID companyId, UUID clientId, Pageable pageable);

    Optional<Invoice> findByIdAndCompanyId(UUID id, UUID companyId);

    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(i.invoiceNumber, 5) AS int)), 0) " +
           "FROM Invoice i WHERE i.company.id = :companyId AND i.invoiceNumber LIKE :prefix%")
    int findMaxInvoiceNumber(@Param("companyId") UUID companyId, @Param("prefix") String prefix);

    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.company.id = :companyId AND i.status = :status")
    long countByCompanyIdAndStatus(@Param("companyId") UUID companyId, @Param("status") String status);

    @Query("SELECT COALESCE(SUM(i.total), 0) FROM Invoice i WHERE i.company.id = :companyId AND i.status = 'paid' " +
           "AND i.issueDate BETWEEN :from AND :to")
    BigDecimal sumPaidByCompanyIdAndDateRange(@Param("companyId") UUID companyId,
                                              @Param("from") LocalDate from,
                                              @Param("to") LocalDate to);

    @Query("SELECT COALESCE(SUM(i.total - i.amountPaid), 0) FROM Invoice i " +
           "WHERE i.company.id = :companyId AND i.status IN ('sent', 'overdue')")
    BigDecimal sumOutstandingByCompanyId(@Param("companyId") UUID companyId);
}
