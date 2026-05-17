package com.invoiceapp.expense;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface ExpenseRepository extends JpaRepository<Expense, UUID> {
    Page<Expense> findByCompanyId(UUID companyId, Pageable pageable);
    Page<Expense> findByCompanyIdAndCategory(UUID companyId, String category, Pageable pageable);
    Optional<Expense> findByIdAndCompanyId(UUID id, UUID companyId);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e " +
           "WHERE e.company.id = :companyId AND e.expenseDate BETWEEN :from AND :to")
    BigDecimal sumByCompanyIdAndDateRange(@Param("companyId") UUID companyId,
                                          @Param("from") LocalDate from, @Param("to") LocalDate to);
}
