package com.invoiceapp.importexport;

import com.invoiceapp.auth.User;
import com.invoiceapp.shared.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Import/Export endpoints — Fase 2.
 * Planned: CSV import of clients/products, CSV/Excel export of invoices/expenses.
 */
@RestController
@RequestMapping("/api/v1/companies/{companyId}/import-export")
@RequiredArgsConstructor
public class ImportExportController {

    @PostMapping("/import/clients")
    public ResponseEntity<ApiResponse<Void>> importClients(
            @PathVariable UUID companyId,
            @AuthenticationPrincipal User user) {
        // TODO: Fase 2 — CSV import
        return ResponseEntity.ok(ApiResponse.ok(null, "Importación de clientes pendiente de implementación (Fase 2)"));
    }

    @GetMapping("/export/invoices")
    public ResponseEntity<ApiResponse<Void>> exportInvoices(
            @PathVariable UUID companyId,
            @AuthenticationPrincipal User user) {
        // TODO: Fase 2 — CSV/Excel export
        return ResponseEntity.ok(ApiResponse.ok(null, "Exportación de facturas pendiente de implementación (Fase 2)"));
    }

    @GetMapping("/export/expenses")
    public ResponseEntity<ApiResponse<Void>> exportExpenses(
            @PathVariable UUID companyId,
            @AuthenticationPrincipal User user) {
        // TODO: Fase 2 — CSV/Excel export
        return ResponseEntity.ok(ApiResponse.ok(null, "Exportación de gastos pendiente de implementación (Fase 2)"));
    }
}
