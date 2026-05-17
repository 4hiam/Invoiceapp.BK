package com.invoiceapp.budget;

import com.invoiceapp.client.Client;
import com.invoiceapp.company.Company;
import com.invoiceapp.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "budgets")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Budget extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(name = "budget_number", nullable = false)
    private String budgetNumber;

    @Column(nullable = false) @Builder.Default
    private String status = "draft";

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "valid_until", nullable = false)
    private LocalDate validUntil;

    @Column(nullable = false, precision = 12, scale = 2) @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "vat_total", nullable = false, precision = 12, scale = 2) @Builder.Default
    private BigDecimal vatTotal = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2) @Builder.Default
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2) @Builder.Default
    private BigDecimal total = BigDecimal.ZERO;

    private String notes;

    @OneToMany(mappedBy = "budget", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BudgetItem> items = new ArrayList<>();

    public void addItem(BudgetItem item) {
        items.add(item);
        item.setBudget(this);
    }

    public void recalculateTotals() {
        this.subtotal = items.stream().map(BudgetItem::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        this.vatTotal = items.stream()
                .map(i -> i.getTotal().multiply(i.getVatRate()).divide(new BigDecimal("100")))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.total = this.subtotal.add(this.vatTotal).subtract(this.discount);
    }
}
