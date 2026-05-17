package com.invoiceapp.client;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID> {

    Page<Client> findByCompanyId(UUID companyId, Pageable pageable);

    Optional<Client> findByIdAndCompanyId(UUID id, UUID companyId);

    long countByCompanyId(UUID companyId);
}
