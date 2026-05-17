package com.invoiceapp.timeentry;

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
@RequestMapping("/api/v1/companies/{companyId}/time-entries")
@RequiredArgsConstructor
public class TimeEntryController {

    private final TimeEntryService timeEntryService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<TimeEntryDTO.Response>>> list(
            @PathVariable UUID companyId, @RequestParam(required = false) UUID clientId,
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 20, sort = "entryDate") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(PagedResponse.from(
                timeEntryService.findAll(companyId, user.getId(), clientId, pageable))));
    }

    @GetMapping("/unbilled")
    public ResponseEntity<ApiResponse<PagedResponse<TimeEntryDTO.Response>>> unbilled(
            @PathVariable UUID companyId, @AuthenticationPrincipal User user,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(PagedResponse.from(
                timeEntryService.findUnbilled(companyId, user.getId(), pageable))));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TimeEntryDTO.Response>> create(
            @PathVariable UUID companyId, @Valid @RequestBody TimeEntryDTO.CreateRequest req,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(timeEntryService.create(companyId, req, user.getId()), "Registro creado"));
    }

    @GetMapping("/{entryId}")
    public ResponseEntity<ApiResponse<TimeEntryDTO.Response>> get(
            @PathVariable UUID companyId, @PathVariable UUID entryId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.ok(timeEntryService.findById(companyId, entryId, user.getId())));
    }

    @PutMapping("/{entryId}")
    public ResponseEntity<ApiResponse<TimeEntryDTO.Response>> update(
            @PathVariable UUID companyId, @PathVariable UUID entryId,
            @Valid @RequestBody TimeEntryDTO.UpdateRequest req, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.ok(
                timeEntryService.update(companyId, entryId, req, user.getId()), "Registro actualizado"));
    }

    @DeleteMapping("/{entryId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable UUID companyId, @PathVariable UUID entryId,
            @AuthenticationPrincipal User user) {
        timeEntryService.delete(companyId, entryId, user.getId());
        return ResponseEntity.ok(ApiResponse.ok(null, "Registro eliminado"));
    }
}
