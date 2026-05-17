package com.invoiceapp.company;

import com.invoiceapp.auth.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;

    public List<CompanyDTO.Response> findAllByUser(UUID userId) {
        return companyRepository.findByUserIdAndIsActiveTrue(userId)
                .stream()
                .map(CompanyDTO::toResponse)
                .toList();
    }

    public CompanyDTO.Response findByIdAndUser(UUID companyId, UUID userId) {
        Company company = getCompanyOrThrow(companyId, userId);
        return CompanyDTO.toResponse(company);
    }

    @Transactional
    public CompanyDTO.Response create(CompanyDTO.CreateRequest request, User user) {
        Company company = Company.builder()
                .user(user)
                .name(request.getName())
                .logoUrl(request.getLogoUrl())
                .address(request.getAddress())
                .taxId(request.getTaxId())
                .currency(request.getCurrency() != null ? request.getCurrency() : "ARS")
                .timezone(request.getTimezone() != null ? request.getTimezone() : "America/Argentina/Jujuy")
                .build();

        company = companyRepository.save(company);
        return CompanyDTO.toResponse(company);
    }

    @Transactional
    public CompanyDTO.Response update(UUID companyId, CompanyDTO.UpdateRequest request, UUID userId) {
        Company company = getCompanyOrThrow(companyId, userId);

        company.setName(request.getName());
        company.setLogoUrl(request.getLogoUrl());
        company.setAddress(request.getAddress());
        company.setTaxId(request.getTaxId());
        if (request.getCurrency() != null) company.setCurrency(request.getCurrency());
        if (request.getTimezone() != null) company.setTimezone(request.getTimezone());

        company = companyRepository.save(company);
        return CompanyDTO.toResponse(company);
    }

    @Transactional
    public void deactivate(UUID companyId, UUID userId) {
        Company company = getCompanyOrThrow(companyId, userId);
        company.setIsActive(false);
        companyRepository.save(company);
    }

    public Company getCompanyOrThrow(UUID companyId, UUID userId) {
        return companyRepository.findByIdAndUserId(companyId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Empresa no encontrada"));
    }
}
