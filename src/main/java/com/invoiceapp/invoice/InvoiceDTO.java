package com.invoiceapp.invoice;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class InvoiceDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotNull(message = "El cliente es requerido")
        private UUID clientId;
        @NotNull(message = "La fecha de emisión es requerida")
        private LocalDate issueDate;
        @NotNull(message = "La fecha de vencimiento es requerida")
        private LocalDate dueDate;
        private String paymentTerms;
        private String notes;
        private BigDecimal discount;

        @NotEmpty(message = "Se requiere al menos un item")
        @Valid
        private List<ItemRequest> items;

        // Toggle recurrente
        private Boolean isRecurring;
        private RecurringConfig recurringConfig;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemRequest {
        private UUID productId;
        @NotBlank(message = "La descripción es requerida")
        private String description;
        @NotNull @Positive
        private Integer quantity;
        @NotNull @Positive
        private BigDecimal unitPrice;
        @NotNull
        private BigDecimal vatRate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecurringConfig {
        @NotBlank(message = "La frecuencia es requerida")
        private String frequency; // monthly, weekly, biweekly, annual
        @NotNull
        private Integer dayOfMonth; // 1-28
        private LocalDate endDate; // nullable = indefinido
        private Boolean autoSend;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        @NotNull private UUID clientId;
        @NotNull private LocalDate issueDate;
        @NotNull private LocalDate dueDate;
        private String paymentTerms;
        private String notes;
        private BigDecimal discount;
        @NotEmpty @Valid
        private List<ItemRequest> items;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusRequest {
        @NotBlank(message = "El estado es requerido")
        private String status; // draft, sent, paid, overdue, cancelled
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private UUID id;
        private UUID companyId;
        private UUID clientId;
        private String clientName;
        private String clientEmail;
        private UUID recurringInvoiceId;
        private Boolean isRecurringSource;
        private String invoiceNumber;
        private String status;
        private LocalDate issueDate;
        private LocalDate dueDate;
        private BigDecimal subtotal;
        private BigDecimal vatTotal;
        private BigDecimal discount;
        private BigDecimal total;
        private BigDecimal amountPaid;
        private BigDecimal amountDue;
        private String paymentTerms;
        private String notes;
        private List<ItemResponse> items;
        private Instant createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemResponse {
        private UUID id;
        private UUID productId;
        private String description;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal vatRate;
        private BigDecimal total;
    }

    public static Response toResponse(Invoice invoice) {
        return Response.builder()
                .id(invoice.getId())
                .companyId(invoice.getCompany().getId())
                .clientId(invoice.getClient().getId())
                .clientName(invoice.getClient().getName())
                .clientEmail(invoice.getClient().getEmail())
                .recurringInvoiceId(invoice.getRecurringInvoiceId())
                .isRecurringSource(invoice.getIsRecurringSource())
                .invoiceNumber(invoice.getInvoiceNumber())
                .status(invoice.getStatus())
                .issueDate(invoice.getIssueDate())
                .dueDate(invoice.getDueDate())
                .subtotal(invoice.getSubtotal())
                .vatTotal(invoice.getVatTotal())
                .discount(invoice.getDiscount())
                .total(invoice.getTotal())
                .amountPaid(invoice.getAmountPaid())
                .amountDue(invoice.getTotal().subtract(invoice.getAmountPaid()))
                .paymentTerms(invoice.getPaymentTerms())
                .notes(invoice.getNotes())
                .items(invoice.getItems().stream().map(InvoiceDTO::toItemResponse).toList())
                .createdAt(invoice.getCreatedAt())
                .build();
    }

    public static ItemResponse toItemResponse(InvoiceItem item) {
        return ItemResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .description(item.getDescription())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .vatRate(item.getVatRate())
                .total(item.getTotal())
                .build();
    }
}
