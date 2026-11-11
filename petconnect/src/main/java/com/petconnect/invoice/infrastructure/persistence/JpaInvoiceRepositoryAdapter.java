package com.petconnect.invoice.infrastructure.persistence;

import com.petconnect.invoice.domain.Invoice;
import com.petconnect.invoice.domain.repositories.InvoiceRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public class JpaInvoiceRepositoryAdapter implements InvoiceRepository {

    private final SpringDataInvoiceRepository repository;

    public JpaInvoiceRepositoryAdapter(SpringDataInvoiceRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Invoice save(Invoice invoice) {
        return repository.save(invoice);
    }

    @Override
    public Invoice findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found: " + id));
    }

    @Override
    public List<Invoice> findByOrderId(UUID orderId) {
        return repository.findByOrderId(orderId);
    }

    @Override
    public boolean existsByInvoiceNumber(String invoiceNumber) {
        return repository.existsByInvoiceNumber(invoiceNumber);
    }
}