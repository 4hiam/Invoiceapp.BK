package com.invoiceapp.budget;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;

public interface BudgetRepository extends JpaRepository<Budget, UUID> {
    Page<Budget> findByCompanyId(UUID companyId, Pageable pageable);
    Optional<Budget> findByIdAndCompanyId(UUID id, UUID companyId);

    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(b.budgetNumber, 5) AS int)), 0) " +
           "FROM Budget b WHERE b.company.id = :companyId AND b.budgetNumber LIKE :prefix%")
    int findMaxBudgetNumber(@Param("companyId") UUID companyId, @Param("prefix") String prefix);
}
