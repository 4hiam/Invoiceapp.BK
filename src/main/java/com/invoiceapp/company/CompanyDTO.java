package com.invoiceapp.company;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

public class CompanyDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotBlank(message = "El nombre es requerido")
        private String name;
        private String logoUrl;
        private String address;
        private String taxId;
        private String currency;
        private String timezone;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        @NotBlank(message = "El nombre es requerido")
        private String name;
        private String logoUrl;
        private String address;
        private String taxId;
        private String currency;
        private String timezone;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private UUID id;
        private String name;
        private String logoUrl;
        private String address;
        private String taxId;
        private String currency;
        private String timezone;
        private Boolean isActive;
        private Instant createdAt;
    }

    public static Response toResponse(Company company) {
        return Response.builder()
                .id(company.getId())
                .name(company.getName())
                .logoUrl(company.getLogoUrl())
                .address(company.getAddress())
                .taxId(company.getTaxId())
                .currency(company.getCurrency())
                .timezone(company.getTimezone())
                .isActive(company.getIsActive())
                .createdAt(company.getCreatedAt())
                .build();
    }
}
