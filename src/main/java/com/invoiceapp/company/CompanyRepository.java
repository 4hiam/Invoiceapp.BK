package com.invoiceapp.company;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CompanyRepository extends JpaRepository<Company, UUID> {

    List<Company> findByUserIdAndIsActiveTrue(UUID userId);

    Optional<Company> findByIdAndUserId(UUID id, UUID userId);
}
