package com.invoiceapp.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class ProductDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotBlank(message = "El nombre es requerido")
        private String name;
        private String description;
        @NotNull(message = "El precio es requerido")
        @Positive(message = "El precio debe ser positivo")
        private BigDecimal price;
        private BigDecimal vatRate;
        private String unit;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        @NotBlank(message = "El nombre es requerido")
        private String name;
        private String description;
        @NotNull(message = "El precio es requerido")
        @Positive(message = "El precio debe ser positivo")
        private BigDecimal price;
        private BigDecimal vatRate;
        private String unit;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private UUID id;
        private String name;
        private String description;
        private BigDecimal price;
        private BigDecimal vatRate;
        private String unit;
        private Boolean isActive;
        private Instant createdAt;
    }

    public static Response toResponse(Product product) {
        return Response.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .vatRate(product.getVatRate())
                .unit(product.getUnit())
                .isActive(product.getIsActive())
                .createdAt(product.getCreatedAt())
                .build();
    }
}
