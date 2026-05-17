package com.invoiceapp.recurring;

import com.invoiceapp.client.Client;
import com.invoiceapp.company.Company;
import com.invoiceapp.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recurring_invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecurringInvoice extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(nullable = false)
    private String frequency; // monthly, weekly, biweekly, annual

    @Column(name = "day_of_month", nullable = false)
    private Integer dayOfMonth;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "auto_send", nullable = false)
    @Builder.Default
    private Boolean autoSend = false;

    @Column(name = "last_notification_status")
    @Builder.Default
    private String lastNotificationStatus = "pending";

    @Column(name = "next_generation_date", nullable = false)
    private Instant nextGenerationDate;

    @OneToMany(mappedBy = "recurringInvoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RecurringInvoiceItem> items = new ArrayList<>();

    public void addItem(RecurringInvoiceItem item) {
        items.add(item);
        item.setRecurringInvoice(this);
    }
}
