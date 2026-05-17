package com.invoiceapp.budget;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class BudgetDTO {

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class CreateRequest {
        @NotNull private UUID clientId;
        @NotNull private LocalDate issueDate;
        @NotNull private LocalDate validUntil;
        private String notes;
        private BigDecimal discount;
        @NotEmpty @Valid private List<ItemRequest> items;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class ItemRequest {
        private UUID productId;
        @NotBlank private String description;
        @NotNull @Positive private Integer quantity;
        @NotNull @Positive private BigDecimal unitPrice;
        @NotNull private BigDecimal vatRate;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class UpdateRequest {
        @NotNull private UUID clientId;
        @NotNull private LocalDate issueDate;
        @NotNull private LocalDate validUntil;
        private String notes;
        private BigDecimal discount;
        @NotEmpty @Valid private List<ItemRequest> items;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class StatusRequest {
        @NotBlank private String status;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Response {
        private UUID id;
        private UUID companyId;
        private UUID clientId;
        private String clientName;
        private String budgetNumber;
        private String status;
        private LocalDate issueDate;
        private LocalDate validUntil;
        private BigDecimal subtotal;
        private BigDecimal vatTotal;
        private BigDecimal discount;
        private BigDecimal total;
        private String notes;
        private List<ItemResponse> items;
        private Instant createdAt;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ItemResponse {
        private UUID id;
        private UUID productId;
        private String description;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal vatRate;
        private BigDecimal total;
    }

    public static Response toResponse(Budget b) {
        return Response.builder()
                .id(b.getId()).companyId(b.getCompany().getId())
                .clientId(b.getClient().getId()).clientName(b.getClient().getName())
                .budgetNumber(b.getBudgetNumber()).status(b.getStatus())
                .issueDate(b.getIssueDate()).validUntil(b.getValidUntil())
                .subtotal(b.getSubtotal()).vatTotal(b.getVatTotal())
                .discount(b.getDiscount()).total(b.getTotal()).notes(b.getNotes())
                .items(b.getItems().stream().map(BudgetDTO::toItemResponse).toList())
                .createdAt(b.getCreatedAt()).build();
    }

    public static ItemResponse toItemResponse(BudgetItem i) {
        return ItemResponse.builder()
                .id(i.getId()).productId(i.getProductId()).description(i.getDescription())
                .quantity(i.getQuantity()).unitPrice(i.getUnitPrice())
                .vatRate(i.getVatRate()).total(i.getTotal()).build();
    }
}
