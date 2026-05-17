package com.invoiceapp.recurring;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class RecurringDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private UUID id;
        private UUID companyId;
        private UUID clientId;
        private String clientName;
        private String frequency;
        private Integer dayOfMonth;
        private LocalDate startDate;
        private LocalDate endDate;
        private Boolean isActive;
        private Boolean autoSend;
        private String lastNotificationStatus;
        private Instant nextGenerationDate;
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
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConfirmAction {
        private String action; // confirm, cancel
    }

    public static Response toResponse(RecurringInvoice ri) {
        return Response.builder()
                .id(ri.getId())
                .companyId(ri.getCompany().getId())
                .clientId(ri.getClient().getId())
                .clientName(ri.getClient().getName())
                .frequency(ri.getFrequency())
                .dayOfMonth(ri.getDayOfMonth())
                .startDate(ri.getStartDate())
                .endDate(ri.getEndDate())
                .isActive(ri.getIsActive())
                .autoSend(ri.getAutoSend())
                .lastNotificationStatus(ri.getLastNotificationStatus())
                .nextGenerationDate(ri.getNextGenerationDate())
                .items(ri.getItems().stream().map(RecurringDTO::toItemResponse).toList())
                .createdAt(ri.getCreatedAt())
                .build();
    }

    public static ItemResponse toItemResponse(RecurringInvoiceItem item) {
        return ItemResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .description(item.getDescription())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .vatRate(item.getVatRate())
                .build();
    }
}
