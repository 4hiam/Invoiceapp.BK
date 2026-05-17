package com.invoiceapp.client;

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
public class ClientService {

    private final ClientRepository clientRepository;
    private final CompanyService companyService;

    public Page<ClientDTO.Response> findAll(UUID companyId, UUID userId, Pageable pageable) {
        companyService.getCompanyOrThrow(companyId, userId);
        return clientRepository.findByCompanyId(companyId, pageable)
                .map(ClientDTO::toResponse);
    }

    public ClientDTO.Response findById(UUID companyId, UUID clientId, UUID userId) {
        companyService.getCompanyOrThrow(companyId, userId);
        Client client = getClientOrThrow(clientId, companyId);
        return ClientDTO.toResponse(client);
    }

    @Transactional
    public ClientDTO.Response create(UUID companyId, ClientDTO.CreateRequest request, UUID userId) {
        Company company = companyService.getCompanyOrThrow(companyId, userId);

        Client client = Client.builder()
                .company(company)
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .taxId(request.getTaxId())
                .notes(request.getNotes())
                .build();

        client = clientRepository.save(client);
        return ClientDTO.toResponse(client);
    }

    @Transactional
    public ClientDTO.Response update(UUID companyId, UUID clientId, ClientDTO.UpdateRequest request, UUID userId) {
        companyService.getCompanyOrThrow(companyId, userId);
        Client client = getClientOrThrow(clientId, companyId);

        client.setName(request.getName());
        client.setEmail(request.getEmail());
        client.setPhone(request.getPhone());
        client.setAddress(request.getAddress());
        client.setTaxId(request.getTaxId());
        client.setNotes(request.getNotes());

        client = clientRepository.save(client);
        return ClientDTO.toResponse(client);
    }

    @Transactional
    public void delete(UUID companyId, UUID clientId, UUID userId) {
        companyService.getCompanyOrThrow(companyId, userId);
        Client client = getClientOrThrow(clientId, companyId);
        clientRepository.delete(client);
    }

    public Client getClientOrThrow(UUID clientId, UUID companyId) {
        return clientRepository.findByIdAndCompanyId(clientId, companyId)
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado"));
    }
}
