package com.invoiceapp.invoice;

import com.invoiceapp.client.Client;
import com.invoiceapp.client.ClientService;
import com.invoiceapp.company.Company;
import com.invoiceapp.company.CompanyService;
import com.invoiceapp.recurring.RecurringInvoiceService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final CompanyService companyService;
    private final ClientService clientService;
    private final RecurringInvoiceService recurringInvoiceService;

    public Page<InvoiceDTO.Response> findAll(UUID companyId, UUID userId, String status, Pageable pageable) {
        companyService.getCompanyOrThrow(companyId, userId);
        Page<Invoice> page;
        if (status != null && !status.isBlank()) {
            page = invoiceRepository.findByCompanyIdAndStatus(companyId, status, pageable);
        } else {
            page = invoiceRepository.findByCompanyId(companyId, pageable);
        }
        return page.map(InvoiceDTO::toResponse);
    }

    public InvoiceDTO.Response findById(UUID companyId, UUID invoiceId, UUID userId) {
        companyService.getCompanyOrThrow(companyId, userId);
        Invoice invoice = getInvoiceOrThrow(invoiceId, companyId);
        return InvoiceDTO.toResponse(invoice);
    }

    @Transactional
    public InvoiceDTO.Response create(UUID companyId, InvoiceDTO.CreateRequest request, UUID userId) {
        Company company = companyService.getCompanyOrThrow(companyId, userId);
        Client client = clientService.getClientOrThrow(request.getClientId(), companyId);

        Invoice invoice = Invoice.builder()
                .company(company)
                .client(client)
                .invoiceNumber(generateInvoiceNumber(companyId))
                .issueDate(request.getIssueDate())
                .dueDate(request.getDueDate())
                .paymentTerms(request.getPaymentTerms())
                .notes(request.getNotes())
                .discount(request.getDiscount() != null ? request.getDiscount() : BigDecimal.ZERO)
                .build();

        // Add items
        for (InvoiceDTO.ItemRequest itemReq : request.getItems()) {
            InvoiceItem item = InvoiceItem.builder()
                    .productId(itemReq.getProductId())
                    .description(itemReq.getDescription())
                    .quantity(itemReq.getQuantity())
                    .unitPrice(itemReq.getUnitPrice())
                    .vatRate(itemReq.getVatRate())
                    .total(itemReq.getUnitPrice().multiply(new BigDecimal(itemReq.getQuantity())))
                    .build();
            invoice.addItem(item);
        }

        invoice.recalculateTotals();

        // Handle recurring toggle
        if (Boolean.TRUE.equals(request.getIsRecurring()) && request.getRecurringConfig() != null) {
            invoice.setIsRecurringSource(true);
        }

        invoice = invoiceRepository.save(invoice);

        // Create recurring invoice record if toggle is on
        if (Boolean.TRUE.equals(request.getIsRecurring()) && request.getRecurringConfig() != null) {
            recurringInvoiceService.createFromInvoice(invoice, request.getRecurringConfig());
        }

        return InvoiceDTO.toResponse(invoice);
    }

    @Transactional
    public InvoiceDTO.Response update(UUID companyId, UUID invoiceId, InvoiceDTO.UpdateRequest request, UUID userId) {
        companyService.getCompanyOrThrow(companyId, userId);
        Invoice invoice = getInvoiceOrThrow(invoiceId, companyId);

        if (!"draft".equals(invoice.getStatus())) {
            throw new IllegalArgumentException("Solo se pueden editar facturas en estado borrador");
        }

        Client client = clientService.getClientOrThrow(request.getClientId(), companyId);
        invoice.setClient(client);
        invoice.setIssueDate(request.getIssueDate());
        invoice.setDueDate(request.getDueDate());
        invoice.setPaymentTerms(request.getPaymentTerms());
        invoice.setNotes(request.getNotes());
        invoice.setDiscount(request.getDiscount() != null ? request.getDiscount() : BigDecimal.ZERO);

        // Replace items
        invoice.getItems().clear();
        for (InvoiceDTO.ItemRequest itemReq : request.getItems()) {
            InvoiceItem item = InvoiceItem.builder()
                    .productId(itemReq.getProductId())
                    .description(itemReq.getDescription())
                    .quantity(itemReq.getQuantity())
                    .unitPrice(itemReq.getUnitPrice())
                    .vatRate(itemReq.getVatRate())
                    .total(itemReq.getUnitPrice().multiply(new BigDecimal(itemReq.getQuantity())))
                    .build();
            invoice.addItem(item);
        }

        invoice.recalculateTotals();
        invoice = invoiceRepository.save(invoice);
        return InvoiceDTO.toResponse(invoice);
    }

    @Transactional
    public InvoiceDTO.Response updateStatus(UUID companyId, UUID invoiceId, String newStatus, UUID userId) {
        companyService.getCompanyOrThrow(companyId, userId);
        Invoice invoice = getInvoiceOrThrow(invoiceId, companyId);
        validateStatusTransition(invoice.getStatus(), newStatus);
        invoice.setStatus(newStatus);
        invoice = invoiceRepository.save(invoice);
        return InvoiceDTO.toResponse(invoice);
    }

    @Transactional
    public InvoiceDTO.Response duplicate(UUID companyId, UUID invoiceId, UUID userId) {
        companyService.getCompanyOrThrow(companyId, userId);
        Invoice original = getInvoiceOrThrow(invoiceId, companyId);

        Invoice copy = Invoice.builder()
                .company(original.getCompany())
                .client(original.getClient())
                .invoiceNumber(generateInvoiceNumber(companyId))
                .status("draft")
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(30))
                .paymentTerms(original.getPaymentTerms())
                .notes(original.getNotes())
                .discount(original.getDiscount())
                .build();

        for (InvoiceItem origItem : original.getItems()) {
            InvoiceItem item = InvoiceItem.builder()
                    .productId(origItem.getProductId())
                    .description(origItem.getDescription())
                    .quantity(origItem.getQuantity())
                    .unitPrice(origItem.getUnitPrice())
                    .vatRate(origItem.getVatRate())
                    .total(origItem.getTotal())
                    .build();
            copy.addItem(item);
        }

        copy.recalculateTotals();
        copy = invoiceRepository.save(copy);
        return InvoiceDTO.toResponse(copy);
    }

    @Transactional
    public void delete(UUID companyId, UUID invoiceId, UUID userId) {
        companyService.getCompanyOrThrow(companyId, userId);
        Invoice invoice = getInvoiceOrThrow(invoiceId, companyId);
        if (!"draft".equals(invoice.getStatus()) && !"cancelled".equals(invoice.getStatus())) {
            throw new IllegalArgumentException("Solo se pueden eliminar facturas en estado borrador o cancelado");
        }
        invoiceRepository.delete(invoice);
    }

    public Invoice getInvoiceOrThrow(UUID invoiceId, UUID companyId) {
        return invoiceRepository.findByIdAndCompanyId(invoiceId, companyId)
                .orElseThrow(() -> new EntityNotFoundException("Factura no encontrada"));
    }

    private String generateInvoiceNumber(UUID companyId) {
        String prefix = "INV-";
        String yearMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMM"));
        String fullPrefix = prefix + yearMonth;
        int maxNum = invoiceRepository.findMaxInvoiceNumber(companyId, fullPrefix);
        return fullPrefix + String.format("%04d", maxNum + 1);
    }

    private void validateStatusTransition(String current, String next) {
        boolean valid = switch (current) {
            case "draft" -> "sent".equals(next) || "cancelled".equals(next);
            case "sent" -> "paid".equals(next) || "overdue".equals(next) || "cancelled".equals(next);
            case "overdue" -> "paid".equals(next) || "cancelled".equals(next);
            default -> false;
        };
        if (!valid) {
            throw new IllegalArgumentException(
                    String.format("Transición de estado inválida: %s → %s", current, next));
        }
    }
}
