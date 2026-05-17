package com.invoiceapp.budget;

import com.invoiceapp.auth.User;
import com.invoiceapp.invoice.InvoiceDTO;
import com.invoiceapp.shared.ApiResponse;
import com.invoiceapp.shared.PagedResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/companies/{companyId}/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<BudgetDTO.Response>>> list(
            @PathVariable UUID companyId, @AuthenticationPrincipal User user,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(PagedResponse.from(
                budgetService.findAll(companyId, user.getId(), pageable))));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BudgetDTO.Response>> create(
            @PathVariable UUID companyId, @Valid @RequestBody BudgetDTO.CreateRequest req,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(budgetService.create(companyId, req, user.getId()), "Presupuesto creado"));
    }

    @GetMapping("/{budgetId}")
    public ResponseEntity<ApiResponse<BudgetDTO.Response>> get(
            @PathVariable UUID companyId, @PathVariable UUID budgetId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.ok(budgetService.findById(companyId, budgetId, user.getId())));
    }

    @PutMapping("/{budgetId}")
    public ResponseEntity<ApiResponse<BudgetDTO.Response>> update(
            @PathVariable UUID companyId, @PathVariable UUID budgetId,
            @Valid @RequestBody BudgetDTO.UpdateRequest req, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.ok(
                budgetService.update(companyId, budgetId, req, user.getId()), "Presupuesto actualizado"));
    }

    @PatchMapping("/{budgetId}/status")
    public ResponseEntity<ApiResponse<BudgetDTO.Response>> updateStatus(
            @PathVariable UUID companyId, @PathVariable UUID budgetId,
            @Valid @RequestBody BudgetDTO.StatusRequest req, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.ok(
                budgetService.updateStatus(companyId, budgetId, req.getStatus(), user.getId())));
    }

    @PostMapping("/{budgetId}/convert-to-invoice")
    public ResponseEntity<ApiResponse<InvoiceDTO.Response>> convertToInvoice(
            @PathVariable UUID companyId, @PathVariable UUID budgetId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(budgetService.convertToInvoice(companyId, budgetId, user.getId()),
                        "Factura creada desde presupuesto"));
    }

    @DeleteMapping("/{budgetId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable UUID companyId, @PathVariable UUID budgetId,
            @AuthenticationPrincipal User user) {
        budgetService.delete(companyId, budgetId, user.getId());
        return ResponseEntity.ok(ApiResponse.ok(null, "Presupuesto eliminado"));
    }
}
