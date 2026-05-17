package com.invoiceapp.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    Page<Product> findByCompanyIdAndIsActiveTrue(UUID companyId, Pageable pageable);

    Optional<Product> findByIdAndCompanyId(UUID id, UUID companyId);
}
