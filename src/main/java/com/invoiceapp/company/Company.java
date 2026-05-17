package com.invoiceapp.company;

import com.invoiceapp.auth.User;
import com.invoiceapp.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "companies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    @Column(name = "logo_url")
    private String logoUrl;

    private String address;

    @Column(name = "tax_id")
    private String taxId;

    @Column(nullable = false)
    @Builder.Default
    private String currency = "ARS";

    @Column(nullable = false)
    @Builder.Default
    private String timezone = "America/Argentina/Jujuy";

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
