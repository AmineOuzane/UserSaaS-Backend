package org.sid.usersaas.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sid.usersaas.enums.ServiceCategory;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Data
@NoArgsConstructor @AllArgsConstructor @Builder
public class UsageType {
    @Id
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_category", nullable = false)
    private ServiceCategory serviceCategory;

    @OneToMany(mappedBy = "usageType", fetch = FetchType.LAZY)
    private List<UsageRecord> usageRecords; // Correct name should probably be usageRecords (plural)

}
