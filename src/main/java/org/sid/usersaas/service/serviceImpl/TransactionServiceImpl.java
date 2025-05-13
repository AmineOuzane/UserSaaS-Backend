package org.sid.usersaas.service.serviceImpl;

import lombok.AllArgsConstructor;
import org.sid.usersaas.entities.AppUser;
import org.sid.usersaas.entities.CreditTransaction;
import org.sid.usersaas.entities.UsageRecord;
import org.sid.usersaas.entities.UsageType;
import org.sid.usersaas.enums.ServiceCategory;
import org.sid.usersaas.enums.TransactionSource;
import org.sid.usersaas.repository.AppUserRepository;
import org.sid.usersaas.repository.CreditTransactionRepository;
import org.sid.usersaas.repository.UsageRecordRepository;
import org.sid.usersaas.repository.UsageTypeRepository;
import org.sid.usersaas.service.TransactionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Transactional
public class TransactionServiceImpl implements TransactionService {

	private AppUserRepository appUserRepository;
    private CreditTransactionRepository creditTransactionRepository;
    private UsageRecordRepository usageRecordRepository;
    private UsageTypeRepository usageTypeRepository;

    @Override
    public BigDecimal getUserWalletBalance(String username) {
        AppUser appUser = appUserRepository.findByUsername(username);
        if (appUser == null) {
            throw new RuntimeException("User not found");
        }
        return appUser.getWalletBalance();
    }

    @Override
    public CreditTransaction addCreditsToUser(String username, BigDecimal amount) {
        AppUser appUser = appUserRepository.findByUsername(username);
        if (appUser == null) {
            throw new RuntimeException("User not found");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        // Update the user's wallet balance
        BigDecimal newBalance = appUser.getWalletBalance().add(amount);
        appUser.setWalletBalance(newBalance);
        appUserRepository.save(appUser);

        return CreditTransaction.builder()
                .amount(amount)
                .balanceAfterTransaction(newBalance)
                .transactionDate(LocalDateTime.now())
                .transactionType(TransactionSource.ADD_FUNDS)
                .appUser(appUser)
                .build();
        // No UsageRecord created because its used only for recording the use of the whatsapp service not the wallet
    }

    @Override
    public CreditTransaction deductCreditsForUsage(String username, String usageTypeId) {
        AppUser appUser = appUserRepository.findByUsername(username);

        UsageType usageType = usageTypeRepository.findById(usageTypeId)
                .orElseThrow(() -> new RuntimeException("Usage type not found with id: " + usageTypeId));
        BigDecimal amount = usageType.getUnitPrice();

        if (appUser == null) {
            throw new RuntimeException("User not found");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (appUser.getWalletBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds in wallet");
        }

        BigDecimal newBalance = appUser.getWalletBalance().subtract(amount);
        appUser.setWalletBalance(newBalance);
        appUserRepository.save(appUser);

        // Create and save the UsageRecord
        UsageRecord usageRecord = UsageRecord.builder()
                .amount(amount)
                .timestamp(LocalDateTime.now())
                .appUser(appUser)
                .usageType(usageTypeRepository.findById(usageTypeId)
                        .orElseThrow(() -> new RuntimeException("Usage type not found")))
                .build();
        usageRecordRepository.save(usageRecord);

        return CreditTransaction.builder()
                .amount(amount)
                .balanceAfterTransaction(newBalance)
                .transactionDate(LocalDateTime.now())
                .transactionType(TransactionSource.PAY_AS_YOU_GO)
                .appUser(appUser)
                .usageRecord(usageRecord)
                .build();
    }

    @Override
    public List<CreditTransaction> getTransactionsForUser(String username, Optional<LocalDateTime> startDate, Optional<LocalDateTime> endDate, Optional<TransactionSource> source) {

        AppUser appUser = appUserRepository.findByUsername(username);
        if (appUser == null) {
            // Use a more specific exception
            throw new RuntimeException("User not found for username: " + username);
        }

        // Keep date validation
        if (startDate.isPresent() && endDate.isPresent() && startDate.get().isAfter(endDate.get())) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        List<CreditTransaction> transactions;

        // Determine which repository method to call based on provided filters
        if (startDate.isPresent() && endDate.isPresent() && source.isPresent()) {
            // Filter by start date, end date, AND source
            transactions = creditTransactionRepository.findByAppUserAndTransactionDateBetweenAndTransactionType(
                    appUser, startDate.get(), endDate.get(), source.get());
        } else if (startDate.isPresent() && endDate.isPresent()) {
            // Filter by start date AND end date
            transactions = creditTransactionRepository.findByAppUserAndTransactionDateBetween(
                    appUser, startDate.get(), endDate.get());
        } else if (startDate.isPresent() && source.isPresent()) {
            // Filter by start date AND source
            transactions = creditTransactionRepository.findByAppUserAndTransactionDateAfterAndTransactionType(
                    appUser, startDate.get(), source.get());
        } else if (startDate.isPresent()) {
            // Filter by start date only
            transactions = creditTransactionRepository.findByAppUserAndTransactionDateAfter(appUser, startDate.get());
        } else if (source.isPresent()) {
            // Filter by source only
            transactions = creditTransactionRepository.findByAppUserAndTransactionType(appUser, source.get());
        } else {
            // No filters provided, get all transactions for the user
            // CAUTION: This can still be slow if user has many transactions!
            // Consider pagination or a default date range if you don't use filters.
            transactions = creditTransactionRepository.findByAppUser(appUser);
        }
        return transactions; // Return the result from the repository
    }

    @Override
    public List<UsageRecord> getRecordsForUser(String username, Optional<LocalDateTime> startDate, Optional<LocalDateTime> endDate, Optional<ServiceCategory> source) {
        AppUser appUser = appUserRepository.findByUsername(username);
        if (appUser == null) {
            // Use a more specific exception
            throw new RuntimeException("User not found for username: " + username);
        }
        if (startDate.isPresent() && endDate.isPresent() && startDate.get().isAfter(endDate.get())) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        List<UsageRecord> records;

        if (startDate.isPresent() && endDate.isPresent() && source.isPresent()) {
            records = usageRecordRepository.findByAppUserAndTimestampBetweenAndUsageType_ServiceCategory(
                    appUser, startDate.get(), endDate.get(), source.get());
        } else if (startDate.isPresent() && endDate.isPresent()) {
            records = usageRecordRepository.findByAppUserAndTimestampBetween(
                    appUser, startDate.get(), endDate.get());
        } else if (startDate.isPresent() && source.isPresent()) {
            records = usageRecordRepository.findByAppUserAndTimestampAfterAndUsageType_ServiceCategory(
                    appUser, startDate.get(), source.get());
        } else if (startDate.isPresent()) {
            records = usageRecordRepository.findByAppUserAndTimestampAfter(appUser, startDate.get());
        } else if (source.isPresent()) {
            records = usageRecordRepository.findByAppUserAndUsageType_ServiceCategory(appUser, source.get());
        } else {
            // No filters, get all records (CAUTION!)
            records = usageRecordRepository.findByAppUser(appUser);
        }

        return records;
    }

}
