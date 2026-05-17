package com.invoiceapp.budget;

import com.invoiceapp.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "budget_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BudgetItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budget_id", nullable = false)
    private Budget budget;

    @Column(name = "product_id")
    private UUID productId;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "vat_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal vatRate;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @PrePersist @PreUpdate
    public void calculateTotal() {
        if (quantity != null && unitPrice != null) {
            this.total = unitPrice.multiply(new BigDecimal(quantity));
        }
    }
}
