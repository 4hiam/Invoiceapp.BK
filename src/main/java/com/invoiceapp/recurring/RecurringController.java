package com.invoiceapp.recurring;

import com.invoiceapp.auth.User;
import com.invoiceapp.invoice.InvoiceDTO;
import com.invoiceapp.shared.ApiResponse;
import com.invoiceapp.shared.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/companies/{companyId}/recurring-invoices")
@RequiredArgsConstructor
public class RecurringController {

    private final RecurringInvoiceService recurringService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<RecurringDTO.Response>>> list(
            @PathVariable UUID companyId,
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 20) Pageable pageable) {
        var page = recurringService.findAll(companyId, user.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.ok(PagedResponse.from(page)));
    }

    @GetMapping("/{recurringId}")
    public ResponseEntity<ApiResponse<RecurringDTO.Response>> get(
            @PathVariable UUID companyId,
            @PathVariable UUID recurringId,
            @AuthenticationPrincipal User user) {
        var response = recurringService.findById(companyId, recurringId, user.getId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/{recurringId}/confirm")
    public ResponseEntity<ApiResponse<InvoiceDTO.Response>> confirm(
            @PathVariable UUID companyId,
            @PathVariable UUID recurringId,
            @AuthenticationPrincipal User user) {
        var response = recurringService.confirm(companyId, recurringId, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, "Factura generada desde recurrencia"));
    }

    @PostMapping("/{recurringId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancel(
            @PathVariable UUID companyId,
            @PathVariable UUID recurringId,
            @AuthenticationPrincipal User user) {
        recurringService.cancel(companyId, recurringId, user.getId());
        return ResponseEntity.ok(ApiResponse.ok(null, "Ciclo cancelado"));
    }

    @DeleteMapping("/{recurringId}")
    public ResponseEntity<ApiResponse<Void>> deactivate(
            @PathVariable UUID companyId,
            @PathVariable UUID recurringId,
            @AuthenticationPrincipal User user) {
        recurringService.deactivate(companyId, recurringId, user.getId());
        return ResponseEntity.ok(ApiResponse.ok(null, "Recurrencia desactivada"));
    }
}
