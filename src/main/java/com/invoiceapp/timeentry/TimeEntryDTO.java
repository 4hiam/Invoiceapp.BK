package com.invoiceapp.timeentry;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class TimeEntryDTO {

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class CreateRequest {
        @NotNull private UUID clientId;
        @NotBlank private String description;
        @NotNull @Positive private Integer durationMinutes;
        private BigDecimal hourlyRate;
        @NotNull private LocalDate entryDate;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class UpdateRequest {
        @NotNull private UUID clientId;
        @NotBlank private String description;
        @NotNull @Positive private Integer durationMinutes;
        private BigDecimal hourlyRate;
        @NotNull private LocalDate entryDate;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Response {
        private UUID id;
        private UUID clientId;
        private String clientName;
        private String description;
        private Integer durationMinutes;
        private BigDecimal hourlyRate;
        private LocalDate entryDate;
        private Boolean isBilled;
        private UUID invoiceId;
        private Instant createdAt;
    }

    public static Response toResponse(TimeEntry te) {
        return Response.builder()
                .id(te.getId()).clientId(te.getClient().getId())
                .clientName(te.getClient().getName())
                .description(te.getDescription()).durationMinutes(te.getDurationMinutes())
                .hourlyRate(te.getHourlyRate()).entryDate(te.getEntryDate())
                .isBilled(te.getIsBilled()).invoiceId(te.getInvoiceId())
                .createdAt(te.getCreatedAt()).build();
    }
}
