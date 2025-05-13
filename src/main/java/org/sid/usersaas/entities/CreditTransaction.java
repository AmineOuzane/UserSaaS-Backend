package org.sid.usersaas.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sid.usersaas.enums.TransactionSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor @AllArgsConstructor @Builder
public class CreditTransaction {
     @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
     private Long Id;

     @Column(name = "amount", nullable = false)
     private BigDecimal amount;

     @Column(name = "balance_after_transaction", nullable = false)
     private BigDecimal balanceAfterTransaction;

     @Column(name = "transaction_date", nullable = false)
     private LocalDateTime transactionDate;

     @Enumerated(EnumType.STRING)
     @Column(name = "transaction_type", nullable = false)
     private TransactionSource transactionType;

     @ManyToOne
     @JoinColumn(name = "user_id", nullable = false)
     private AppUser appUser;

    @OneToOne
    @JoinColumn(name = "usage_record_id")
    private UsageRecord usageRecord;

}
