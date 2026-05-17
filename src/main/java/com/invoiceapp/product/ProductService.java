package com.invoiceapp.product;

import com.invoiceapp.company.Company;
import com.invoiceapp.company.CompanyService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CompanyService companyService;

    public Page<ProductDTO.Response> findAll(UUID companyId, UUID userId, Pageable pageable) {
        companyService.getCompanyOrThrow(companyId, userId);
        return productRepository.findByCompanyIdAndIsActiveTrue(companyId, pageable)
                .map(ProductDTO::toResponse);
    }

    public ProductDTO.Response findById(UUID companyId, UUID productId, UUID userId) {
        companyService.getCompanyOrThrow(companyId, userId);
        Product product = getProductOrThrow(productId, companyId);
        return ProductDTO.toResponse(product);
    }

    @Transactional
    public ProductDTO.Response create(UUID companyId, ProductDTO.CreateRequest request, UUID userId) {
        Company company = companyService.getCompanyOrThrow(companyId, userId);

        Product product = Product.builder()
                .company(company)
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .vatRate(request.getVatRate() != null ? request.getVatRate() : new BigDecimal("21.0"))
                .unit(request.getUnit() != null ? request.getUnit() : "unidad")
                .build();

        product = productRepository.save(product);
        return ProductDTO.toResponse(product);
    }

    @Transactional
    public ProductDTO.Response update(UUID companyId, UUID productId, ProductDTO.UpdateRequest request, UUID userId) {
        companyService.getCompanyOrThrow(companyId, userId);
        Product product = getProductOrThrow(productId, companyId);

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        if (request.getVatRate() != null) product.setVatRate(request.getVatRate());
        if (request.getUnit() != null) product.setUnit(request.getUnit());

        product = productRepository.save(product);
        return ProductDTO.toResponse(product);
    }

    @Transactional
    public void deactivate(UUID companyId, UUID productId, UUID userId) {
        companyService.getCompanyOrThrow(companyId, userId);
        Product product = getProductOrThrow(productId, companyId);
        product.setIsActive(false);
        productRepository.save(product);
    }

    public Product getProductOrThrow(UUID productId, UUID companyId) {
        return productRepository.findByIdAndCompanyId(productId, companyId)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));
    }
}
