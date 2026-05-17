package com.invoiceapp.timeentry;

import com.invoiceapp.client.Client;
import com.invoiceapp.client.ClientService;
import com.invoiceapp.company.Company;
import com.invoiceapp.company.CompanyService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TimeEntryService {

    private final TimeEntryRepository timeEntryRepository;
    private final CompanyService companyService;
    private final ClientService clientService;

    public Page<TimeEntryDTO.Response> findAll(UUID companyId, UUID userId, UUID clientId, Pageable pageable) {
        companyService.getCompanyOrThrow(companyId, userId);
        if (clientId != null) {
            return timeEntryRepository.findByCompanyIdAndClientId(companyId, clientId, pageable)
                    .map(TimeEntryDTO::toResponse);
        }
        return timeEntryRepository.findByCompanyId(companyId, pageable).map(TimeEntryDTO::toResponse);
    }

    public Page<TimeEntryDTO.Response> findUnbilled(UUID companyId, UUID userId, Pageable pageable) {
        companyService.getCompanyOrThrow(companyId, userId);
        return timeEntryRepository.findByCompanyIdAndIsBilledFalse(companyId, pageable)
                .map(TimeEntryDTO::toResponse);
    }

    public TimeEntryDTO.Response findById(UUID companyId, UUID entryId, UUID userId) {
        companyService.getCompanyOrThrow(companyId, userId);
        return TimeEntryDTO.toResponse(getEntryOrThrow(entryId, companyId));
    }

    @Transactional
    public TimeEntryDTO.Response create(UUID companyId, TimeEntryDTO.CreateRequest req, UUID userId) {
        Company company = companyService.getCompanyOrThrow(companyId, userId);
        Client client = clientService.getClientOrThrow(req.getClientId(), companyId);
        TimeEntry entry = TimeEntry.builder()
                .company(company).client(client)
                .description(req.getDescription()).durationMinutes(req.getDurationMinutes())
                .hourlyRate(req.getHourlyRate()).entryDate(req.getEntryDate())
                .build();
        return TimeEntryDTO.toResponse(timeEntryRepository.save(entry));
    }

    @Transactional
    public TimeEntryDTO.Response update(UUID companyId, UUID entryId, TimeEntryDTO.UpdateRequest req, UUID userId) {
        companyService.getCompanyOrThrow(companyId, userId);
        TimeEntry entry = getEntryOrThrow(entryId, companyId);
        Client client = clientService.getClientOrThrow(req.getClientId(), companyId);
        entry.setClient(client);
        entry.setDescription(req.getDescription());
        entry.setDurationMinutes(req.getDurationMinutes());
        entry.setHourlyRate(req.getHourlyRate());
        entry.setEntryDate(req.getEntryDate());
        return TimeEntryDTO.toResponse(timeEntryRepository.save(entry));
    }

    @Transactional
    public void delete(UUID companyId, UUID entryId, UUID userId) {
        companyService.getCompanyOrThrow(companyId, userId);
        timeEntryRepository.delete(getEntryOrThrow(entryId, companyId));
    }

    private TimeEntry getEntryOrThrow(UUID entryId, UUID companyId) {
        return timeEntryRepository.findByIdAndCompanyId(entryId, companyId)
                .orElseThrow(() -> new EntityNotFoundException("Registro de tiempo no encontrado"));
    }
}
