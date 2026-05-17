package com.invoiceapp.expense;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class ExpenseDTO {

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class CreateRequest {
        @NotBlank private String category;
        @NotBlank private String description;
        @NotNull @Positive private BigDecimal amount;
        private BigDecimal taxAmount;
        @NotNull private LocalDate expenseDate;
        private String receiptUrl;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class UpdateRequest {
        @NotBlank private String category;
        @NotBlank private String description;
        @NotNull @Positive private BigDecimal amount;
        private BigDecimal taxAmount;
        @NotNull private LocalDate expenseDate;
        private String receiptUrl;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Response {
        private UUID id;
        private String category;
        private String description;
        private BigDecimal amount;
        private BigDecimal taxAmount;
        private LocalDate expenseDate;
        private String receiptUrl;
        private Instant createdAt;
    }

    public static Response toResponse(Expense e) {
        return Response.builder()
                .id(e.getId()).category(e.getCategory()).description(e.getDescription())
                .amount(e.getAmount()).taxAmount(e.getTaxAmount())
                .expenseDate(e.getExpenseDate()).receiptUrl(e.getReceiptUrl())
                .createdAt(e.getCreatedAt()).build();
    }
}
