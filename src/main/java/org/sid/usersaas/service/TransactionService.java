package org.sid.usersaas.service;

import org.sid.usersaas.entities.CreditTransaction;
import org.sid.usersaas.entities.UsageRecord;
import org.sid.usersaas.enums.ServiceCategory;
import org.sid.usersaas.enums.TransactionSource;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionService {

    BigDecimal getUserWalletBalance(String username); // Or use userId
    CreditTransaction addCreditsToUser(String username, BigDecimal amount);
    CreditTransaction deductCreditsForUsage(String username, String usageTypeId); // Link to usage record
    List<CreditTransaction> getTransactionsForUser(String username, Optional<LocalDateTime> startDate, Optional<LocalDateTime> endDate, Optional<TransactionSource> source);
    List<UsageRecord> getRecordsForUser(String username, Optional<LocalDateTime> startDate, Optional<LocalDateTime> endDate, Optional<ServiceCategory> source);

    // Need to use pagination for large data sets to avoid performance issues and memory overload
}
