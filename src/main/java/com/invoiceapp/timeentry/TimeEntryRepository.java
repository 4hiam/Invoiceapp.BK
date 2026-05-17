package com.invoiceapp.timeentry;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface TimeEntryRepository extends JpaRepository<TimeEntry, UUID> {
    Page<TimeEntry> findByCompanyId(UUID companyId, Pageable pageable);
    Page<TimeEntry> findByCompanyIdAndIsBilledFalse(UUID companyId, Pageable pageable);
    Page<TimeEntry> findByCompanyIdAndClientId(UUID companyId, UUID clientId, Pageable pageable);
    Optional<TimeEntry> findByIdAndCompanyId(UUID id, UUID companyId);
}
