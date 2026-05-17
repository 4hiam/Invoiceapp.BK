package com.invoiceapp.client;

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
@RequestMapping("/api/v1/companies/{companyId}/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<ClientDTO.Response>>> list(
            @PathVariable UUID companyId,
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        var page = clientService.findAll(companyId, user.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.ok(PagedResponse.from(page)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ClientDTO.Response>> create(
            @PathVariable UUID companyId,
            @Valid @RequestBody ClientDTO.CreateRequest request,
            @AuthenticationPrincipal User user) {
        var response = clientService.create(companyId, request, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response, "Cliente creado"));
    }

    @GetMapping("/{clientId}")
    public ResponseEntity<ApiResponse<ClientDTO.Response>> get(
            @PathVariable UUID companyId,
            @PathVariable UUID clientId,
            @AuthenticationPrincipal User user) {
        var response = clientService.findById(companyId, clientId, user.getId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{clientId}")
    public ResponseEntity<ApiResponse<ClientDTO.Response>> update(
            @PathVariable UUID companyId,
            @PathVariable UUID clientId,
            @Valid @RequestBody ClientDTO.UpdateRequest request,
            @AuthenticationPrincipal User user) {
        var response = clientService.update(companyId, clientId, request, user.getId());
        return ResponseEntity.ok(ApiResponse.ok(response, "Cliente actualizado"));
    }

    @DeleteMapping("/{clientId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable UUID companyId,
            @PathVariable UUID clientId,
            @AuthenticationPrincipal User user) {
        clientService.delete(companyId, clientId, user.getId());
        return ResponseEntity.ok(ApiResponse.ok(null, "Cliente eliminado"));
    }
}
