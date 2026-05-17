package com.invoiceapp.client;

import com.invoiceapp.company.Company;
import com.invoiceapp.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "clients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false)
    private String name;

    private String email;
    private String phone;
    private String address;

    @Column(name = "tax_id")
    private String taxId;

    private String notes;
}
