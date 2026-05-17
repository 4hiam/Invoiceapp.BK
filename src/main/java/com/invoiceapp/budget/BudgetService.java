package com.invoiceapp.budget;

import com.invoiceapp.client.Client;
import com.invoiceapp.client.ClientService;
import com.invoiceapp.company.Company;
import com.invoiceapp.company.CompanyService;
import com.invoiceapp.invoice.*;
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
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final InvoiceRepository invoiceRepository;
    private final CompanyService companyService;
    private final ClientService clientService;

    public Page<BudgetDTO.Response> findAll(UUID companyId, UUID userId, Pageable pageable) {
        companyService.getCompanyOrThrow(companyId, userId);
        return budgetRepository.findByCompanyId(companyId, pageable).map(BudgetDTO::toResponse);
    }

    public BudgetDTO.Response findById(UUID companyId, UUID budgetId, UUID userId) {
        companyService.getCompanyOrThrow(companyId, userId);
        return BudgetDTO.toResponse(getBudgetOrThrow(budgetId, companyId));
    }

    @Transactional
    public BudgetDTO.Response create(UUID companyId, BudgetDTO.CreateRequest req, UUID userId) {
        Company company = companyService.getCompanyOrThrow(companyId, userId);
        Client client = clientService.getClientOrThrow(req.getClientId(), companyId);

        Budget budget = Budget.builder()
                .company(company).client(client)
                .budgetNumber(generateBudgetNumber(companyId))
                .issueDate(req.getIssueDate()).validUntil(req.getValidUntil())
                .notes(req.getNotes())
                .discount(req.getDiscount() != null ? req.getDiscount() : BigDecimal.ZERO)
                .build();

        for (BudgetDTO.ItemRequest ir : req.getItems()) {
            BudgetItem item = BudgetItem.builder()
                    .productId(ir.getProductId()).description(ir.getDescription())
                    .quantity(ir.getQuantity()).unitPrice(ir.getUnitPrice())
                    .vatRate(ir.getVatRate())
                    .total(ir.getUnitPrice().multiply(new BigDecimal(ir.getQuantity())))
                    .build();
            budget.addItem(item);
        }
        budget.recalculateTotals();
        return BudgetDTO.toResponse(budgetRepository.save(budget));
    }

    @Transactional
    public BudgetDTO.Response update(UUID companyId, UUID budgetId, BudgetDTO.UpdateRequest req, UUID userId) {
        companyService.getCompanyOrThrow(companyId, userId);
        Budget budget = getBudgetOrThrow(budgetId, companyId);
        if (!"draft".equals(budget.getStatus())) {
            throw new IllegalArgumentException("Solo se pueden editar presupuestos en borrador");
        }

        Client client = clientService.getClientOrThrow(req.getClientId(), companyId);
        budget.setClient(client);
        budget.setIssueDate(req.getIssueDate());
        budget.setValidUntil(req.getValidUntil());
        budget.setNotes(req.getNotes());
        budget.setDiscount(req.getDiscount() != null ? req.getDiscount() : BigDecimal.ZERO);

        budget.getItems().clear();
        for (BudgetDTO.ItemRequest ir : req.getItems()) {
            BudgetItem item = BudgetItem.builder()
                    .productId(ir.getProductId()).description(ir.getDescription())
                    .quantity(ir.getQuantity()).unitPrice(ir.getUnitPrice())
                    .vatRate(ir.getVatRate())
                    .total(ir.getUnitPrice().multiply(new BigDecimal(ir.getQuantity())))
                    .build();
            budget.addItem(item);
        }
        budget.recalculateTotals();
        return BudgetDTO.toResponse(budgetRepository.save(budget));
    }

    @Transactional
    public BudgetDTO.Response updateStatus(UUID companyId, UUID budgetId, String status, UUID userId) {
        companyService.getCompanyOrThrow(companyId, userId);
        Budget budget = getBudgetOrThrow(budgetId, companyId);
        budget.setStatus(status);
        return BudgetDTO.toResponse(budgetRepository.save(budget));
    }

    /**
     * Convert accepted budget → invoice
     */
    @Transactional
    public InvoiceDTO.Response convertToInvoice(UUID companyId, UUID budgetId, UUID userId) {
        companyService.getCompanyOrThrow(companyId, userId);
        Budget budget = getBudgetOrThrow(budgetId, companyId);

        if (!"accepted".equals(budget.getStatus())) {
            throw new IllegalArgumentException("Solo se pueden convertir presupuestos aceptados");
        }

        String invNumber = "INV-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyMM"));
        int max = invoiceRepository.findMaxInvoiceNumber(companyId, invNumber);
        invNumber += String.format("%04d", max + 1);

        Invoice invoice = Invoice.builder()
                .company(budget.getCompany()).client(budget.getClient())
                .invoiceNumber(invNumber).status("draft")
                .issueDate(LocalDate.now()).dueDate(LocalDate.now().plusDays(30))
                .discount(budget.getDiscount()).notes(budget.getNotes())
                .build();

        for (BudgetItem bi : budget.getItems()) {
            InvoiceItem ii = InvoiceItem.builder()
                    .productId(bi.getProductId()).description(bi.getDescription())
                    .quantity(bi.getQuantity()).unitPrice(bi.getUnitPrice())
                    .vatRate(bi.getVatRate()).total(bi.getTotal())
                    .build();
            invoice.addItem(ii);
        }
        invoice.recalculateTotals();
        invoice = invoiceRepository.save(invoice);
        return InvoiceDTO.toResponse(invoice);
    }

    @Transactional
    public void delete(UUID companyId, UUID budgetId, UUID userId) {
        companyService.getCompanyOrThrow(companyId, userId);
        Budget budget = getBudgetOrThrow(budgetId, companyId);
        budgetRepository.delete(budget);
    }

    private Budget getBudgetOrThrow(UUID budgetId, UUID companyId) {
        return budgetRepository.findByIdAndCompanyId(budgetId, companyId)
                .orElseThrow(() -> new EntityNotFoundException("Presupuesto no encontrado"));
    }

    private String generateBudgetNumber(UUID companyId) {
        String prefix = "PRE-";
        String ym = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMM"));
        String full = prefix + ym;
        int max = budgetRepository.findMaxBudgetNumber(companyId, full);
        return full + String.format("%04d", max + 1);
    }
}
