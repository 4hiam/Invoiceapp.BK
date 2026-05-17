package com.invoiceapp.product;

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
@RequestMapping("/api/v1/companies/{companyId}/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<ProductDTO.Response>>> list(
            @PathVariable UUID companyId,
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        var page = productService.findAll(companyId, user.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.ok(PagedResponse.from(page)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductDTO.Response>> create(
            @PathVariable UUID companyId,
            @Valid @RequestBody ProductDTO.CreateRequest request,
            @AuthenticationPrincipal User user) {
        var response = productService.create(companyId, request, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response, "Producto creado"));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductDTO.Response>> get(
            @PathVariable UUID companyId,
            @PathVariable UUID productId,
            @AuthenticationPrincipal User user) {
        var response = productService.findById(companyId, productId, user.getId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductDTO.Response>> update(
            @PathVariable UUID companyId,
            @PathVariable UUID productId,
            @Valid @RequestBody ProductDTO.UpdateRequest request,
            @AuthenticationPrincipal User user) {
        var response = productService.update(companyId, productId, request, user.getId());
        return ResponseEntity.ok(ApiResponse.ok(response, "Producto actualizado"));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> deactivate(
            @PathVariable UUID companyId,
            @PathVariable UUID productId,
            @AuthenticationPrincipal User user) {
        productService.deactivate(companyId, productId, user.getId());
        return ResponseEntity.ok(ApiResponse.ok(null, "Producto desactivado"));
    }
}
