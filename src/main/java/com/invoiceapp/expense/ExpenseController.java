package com.invoiceapp.expense;

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
@RequestMapping("/api/v1/companies/{companyId}/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<ExpenseDTO.Response>>> list(
            @PathVariable UUID companyId, @RequestParam(required = false) String category,
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 20, sort = "expenseDate") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(PagedResponse.from(
                expenseService.findAll(companyId, user.getId(), category, pageable))));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ExpenseDTO.Response>> create(
            @PathVariable UUID companyId, @Valid @RequestBody ExpenseDTO.CreateRequest req,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(expenseService.create(companyId, req, user.getId()), "Gasto creado"));
    }

    @GetMapping("/{expenseId}")
    public ResponseEntity<ApiResponse<ExpenseDTO.Response>> get(
            @PathVariable UUID companyId, @PathVariable UUID expenseId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.ok(expenseService.findById(companyId, expenseId, user.getId())));
    }

    @PutMapping("/{expenseId}")
    public ResponseEntity<ApiResponse<ExpenseDTO.Response>> update(
            @PathVariable UUID companyId, @PathVariable UUID expenseId,
            @Valid @RequestBody ExpenseDTO.UpdateRequest req, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.ok(
                expenseService.update(companyId, expenseId, req, user.getId()), "Gasto actualizado"));
    }

    @DeleteMapping("/{expenseId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable UUID companyId, @PathVariable UUID expenseId,
            @AuthenticationPrincipal User user) {
        expenseService.delete(companyId, expenseId, user.getId());
        return ResponseEntity.ok(ApiResponse.ok(null, "Gasto eliminado"));
    }
}
