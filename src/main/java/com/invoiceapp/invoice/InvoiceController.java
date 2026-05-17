package com.invoiceapp.invoice;

import com.invoiceapp.auth.User;
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
@RequestMapping("/api/v1/companies/{companyId}/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<InvoiceDTO.Response>>> list(
            @PathVariable UUID companyId,
            @RequestParam(required = false) String status,
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        var page = invoiceService.findAll(companyId, user.getId(), status, pageable);
        return ResponseEntity.ok(ApiResponse.ok(PagedResponse.from(page)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<InvoiceDTO.Response>> create(
            @PathVariable UUID companyId,
            @Valid @RequestBody InvoiceDTO.CreateRequest request,
            @AuthenticationPrincipal User user) {
        var response = invoiceService.create(companyId, request, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response, "Factura creada"));
    }

    @GetMapping("/{invoiceId}")
    public ResponseEntity<ApiResponse<InvoiceDTO.Response>> get(
            @PathVariable UUID companyId,
            @PathVariable UUID invoiceId,
            @AuthenticationPrincipal User user) {
        var response = invoiceService.findById(companyId, invoiceId, user.getId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{invoiceId}")
    public ResponseEntity<ApiResponse<InvoiceDTO.Response>> update(
            @PathVariable UUID companyId,
            @PathVariable UUID invoiceId,
            @Valid @RequestBody InvoiceDTO.UpdateRequest request,
            @AuthenticationPrincipal User user) {
        var response = invoiceService.update(companyId, invoiceId, request, user.getId());
        return ResponseEntity.ok(ApiResponse.ok(response, "Factura actualizada"));
    }

    @PatchMapping("/{invoiceId}/status")
    public ResponseEntity<ApiResponse<InvoiceDTO.Response>> updateStatus(
            @PathVariable UUID companyId,
            @PathVariable UUID invoiceId,
            @Valid @RequestBody InvoiceDTO.StatusRequest request,
            @AuthenticationPrincipal User user) {
        var response = invoiceService.updateStatus(companyId, invoiceId, request.getStatus(), user.getId());
        return ResponseEntity.ok(ApiResponse.ok(response, "Estado actualizado"));
    }

    @PostMapping("/{invoiceId}/duplicate")
    public ResponseEntity<ApiResponse<InvoiceDTO.Response>> duplicate(
            @PathVariable UUID companyId,
            @PathVariable UUID invoiceId,
            @AuthenticationPrincipal User user) {
        var response = invoiceService.duplicate(companyId, invoiceId, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response, "Factura duplicada"));
    }

    @DeleteMapping("/{invoiceId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable UUID companyId,
            @PathVariable UUID invoiceId,
            @AuthenticationPrincipal User user) {
        invoiceService.delete(companyId, invoiceId, user.getId());
        return ResponseEntity.ok(ApiResponse.ok(null, "Factura eliminada"));
    }
}
