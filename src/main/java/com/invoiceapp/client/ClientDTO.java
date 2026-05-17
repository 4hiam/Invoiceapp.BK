package com.invoiceapp.client;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

public class ClientDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotBlank(message = "El nombre es requerido")
        private String name;
        private String email;
        private String phone;
        private String address;
        private String taxId;
        private String notes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        @NotBlank(message = "El nombre es requerido")
        private String name;
        private String email;
        private String phone;
        private String address;
        private String taxId;
        private String notes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private UUID id;
        private String name;
        private String email;
        private String phone;
        private String address;
        private String taxId;
        private String notes;
        private Instant createdAt;
    }

    public static Response toResponse(Client client) {
        return Response.builder()
                .id(client.getId())
                .name(client.getName())
                .email(client.getEmail())
                .phone(client.getPhone())
                .address(client.getAddress())
                .taxId(client.getTaxId())
                .notes(client.getNotes())
                .createdAt(client.getCreatedAt())
                .build();
    }
}
