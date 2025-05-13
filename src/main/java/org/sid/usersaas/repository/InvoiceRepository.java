package org.sid.usersaas.repository;

import org.sid.usersaas.entities.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    // Custom query methods can be defined here if needed
    // For example, find by userId or date range
    // List<Invoice> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
}
