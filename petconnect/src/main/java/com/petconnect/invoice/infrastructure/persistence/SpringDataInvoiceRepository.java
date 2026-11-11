package com.petconnect.invoice.infrastructure.persistence;

import com.petconnect.invoice.domain.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataInvoiceRepository extends JpaRepository<Invoice, UUID> {
    List<Invoice> findByOrderId(UUID orderId);
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    boolean existsByInvoiceNumber(String invoiceNumber);
}