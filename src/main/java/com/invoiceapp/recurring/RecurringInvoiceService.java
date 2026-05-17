package com.invoiceapp.recurring;

import com.invoiceapp.company.CompanyService;
import com.invoiceapp.invoice.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecurringInvoiceService {

    private final RecurringInvoiceRepository recurringRepository;
    private final InvoiceRepository invoiceRepository;
    private final CompanyService companyService;

    public Page<RecurringDTO.Response> findAll(UUID companyId, UUID userId, Pageable pageable) {
        companyService.getCompanyOrThrow(companyId, userId);
        return recurringRepository.findByCompanyIdAndIsActiveTrue(companyId, pageable)
                .map(RecurringDTO::toResponse);
    }

    public RecurringDTO.Response findById(UUID companyId, UUID recurringId, UUID userId) {
        companyService.getCompanyOrThrow(companyId, userId);
        RecurringInvoice ri = getRecurringOrThrow(recurringId, companyId);
        return RecurringDTO.toResponse(ri);
    }

    /**
     * Called from InvoiceService when user toggles "recurring" on invoice creation.
     * Creates the RecurringInvoice + snapshot items from the invoice items.
     */
    @Transactional
    public void createFromInvoice(Invoice invoice, InvoiceDTO.RecurringConfig config) {
        Instant nextGen = calculateNextGenerationDate(config.getFrequency(), config.getDayOfMonth());

        RecurringInvoice ri = RecurringInvoice.builder()
                .company(invoice.getCompany())
                .client(invoice.getClient())
                .frequency(config.getFrequency())
                .dayOfMonth(config.getDayOfMonth())
                .startDate(invoice.getIssueDate())
                .endDate(config.getEndDate())
                .autoSend(config.getAutoSend() != null ? config.getAutoSend() : false)
                .nextGenerationDate(nextGen)
                .build();

        // Snapshot items from invoice
        for (InvoiceItem item : invoice.getItems()) {
            RecurringInvoiceItem riItem = RecurringInvoiceItem.builder()
                    .productId(item.getProductId())
                    .description(item.getDescription())
                    .quantity(item.getQuantity())
                    .unitPrice(item.getUnitPrice())
                    .vatRate(item.getVatRate())
                    .build();
            ri.addItem(riItem);
        }

        ri = recurringRepository.save(ri);

        // Link invoice to recurring
        invoice.setRecurringInvoiceId(ri.getId());
        invoiceRepository.save(invoice);
    }

    /**
     * User confirms recurring notification → generate the invoice.
     */
    @Transactional
    public InvoiceDTO.Response confirm(UUID companyId, UUID recurringId, UUID userId) {
        companyService.getCompanyOrThrow(companyId, userId);
        RecurringInvoice ri = getRecurringOrThrow(recurringId, companyId);

        if (!"sent".equals(ri.getLastNotificationStatus())) {
            throw new IllegalArgumentException("No hay notificación pendiente de confirmación");
        }

        // Generate invoice from snapshot
        Invoice invoice = generateInvoiceFromRecurring(ri);
        invoice = invoiceRepository.save(invoice);

        // Advance to next cycle
        ri.setLastNotificationStatus("pending");
        ri.setNextGenerationDate(calculateNextGenerationDate(ri.getFrequency(), ri.getDayOfMonth()));
        checkAndDeactivateIfExpired(ri);
        recurringRepository.save(ri);

        log.info("Recurring {} confirmed → Invoice {} generated", recurringId, invoice.getId());
        return InvoiceDTO.toResponse(invoice);
    }

    /**
     * User cancels the recurring notification for this cycle.
     */
    @Transactional
    public void cancel(UUID companyId, UUID recurringId, UUID userId) {
        companyService.getCompanyOrThrow(companyId, userId);
        RecurringInvoice ri = getRecurringOrThrow(recurringId, companyId);

        ri.setLastNotificationStatus("pending");
        ri.setNextGenerationDate(calculateNextGenerationDate(ri.getFrequency(), ri.getDayOfMonth()));
        checkAndDeactivateIfExpired(ri);
        recurringRepository.save(ri);

        log.info("Recurring {} cancelled for this cycle", recurringId);
    }

    /**
     * Deactivate a recurring invoice entirely.
     */
    @Transactional
    public void deactivate(UUID companyId, UUID recurringId, UUID userId) {
        companyService.getCompanyOrThrow(companyId, userId);
        RecurringInvoice ri = getRecurringOrThrow(recurringId, companyId);
        ri.setIsActive(false);
        recurringRepository.save(ri);
    }

    public RecurringInvoice getRecurringOrThrow(UUID recurringId, UUID companyId) {
        return recurringRepository.findByIdAndCompanyId(recurringId, companyId)
                .orElseThrow(() -> new EntityNotFoundException("Factura recurrente no encontrada"));
    }

    private Invoice generateInvoiceFromRecurring(RecurringInvoice ri) {
        String invoiceNumber = generateInvoiceNumber(ri.getCompany().getId());
        LocalDate today = LocalDate.now();

        Invoice invoice = Invoice.builder()
                .company(ri.getCompany())
                .client(ri.getClient())
                .recurringInvoiceId(ri.getId())
                .isRecurringSource(false)
                .invoiceNumber(invoiceNumber)
                .status("draft")
                .issueDate(today)
                .dueDate(today.plusDays(30))
                .build();

        for (RecurringInvoiceItem riItem : ri.getItems()) {
            InvoiceItem item = InvoiceItem.builder()
                    .productId(riItem.getProductId())
                    .description(riItem.getDescription())
                    .quantity(riItem.getQuantity())
                    .unitPrice(riItem.getUnitPrice())
                    .vatRate(riItem.getVatRate())
                    .total(riItem.getUnitPrice().multiply(new BigDecimal(riItem.getQuantity())))
                    .build();
            invoice.addItem(item);
        }

        invoice.recalculateTotals();
        return invoice;
    }

    private String generateInvoiceNumber(UUID companyId) {
        String prefix = "INV-";
        String yearMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMM"));
        String fullPrefix = prefix + yearMonth;
        int maxNum = invoiceRepository.findMaxInvoiceNumber(companyId, fullPrefix);
        return fullPrefix + String.format("%04d", maxNum + 1);
    }

    private Instant calculateNextGenerationDate(String frequency, int dayOfMonth) {
        LocalDate now = LocalDate.now();
        LocalDate next = switch (frequency) {
            case "weekly" -> now.plusWeeks(1);
            case "biweekly" -> now.plusWeeks(2);
            case "monthly" -> {
                LocalDate candidate = now.withDayOfMonth(Math.min(dayOfMonth, now.lengthOfMonth()));
                yield candidate.isAfter(now) ? candidate : candidate.plusMonths(1);
            }
            case "annual" -> {
                LocalDate candidate = now.withDayOfMonth(Math.min(dayOfMonth, now.lengthOfMonth()));
                yield candidate.isAfter(now) ? candidate : candidate.plusYears(1);
            }
            default -> throw new IllegalArgumentException("Frecuencia no válida: " + frequency);
        };
        return next.atStartOfDay(ZoneId.of("America/Argentina/Jujuy")).toInstant();
    }

    private void checkAndDeactivateIfExpired(RecurringInvoice ri) {
        if (ri.getEndDate() != null && LocalDate.now().isAfter(ri.getEndDate())) {
            ri.setIsActive(false);
        }
    }
}
