package com.invoiceapp.company;

import com.invoiceapp.auth.User;
import com.invoiceapp.shared.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CompanyDTO.Response>>> list(
            @AuthenticationPrincipal User user) {
        var companies = companyService.findAllByUser(user.getId());
        return ResponseEntity.ok(ApiResponse.ok(companies));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CompanyDTO.Response>> create(
            @Valid @RequestBody CompanyDTO.CreateRequest request,
            @AuthenticationPrincipal User user) {
        var response = companyService.create(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response, "Empresa creada"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CompanyDTO.Response>> get(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        var response = companyService.findByIdAndUser(id, user.getId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CompanyDTO.Response>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CompanyDTO.UpdateRequest request,
            @AuthenticationPrincipal User user) {
        var response = companyService.update(id, request, user.getId());
        return ResponseEntity.ok(ApiResponse.ok(response, "Empresa actualizada"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deactivate(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        companyService.deactivate(id, user.getId());
        return ResponseEntity.ok(ApiResponse.ok(null, "Empresa desactivada"));
    }
}
