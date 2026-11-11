package com.petconnect.invoice.domain.repositories;

import com.petconnect.invoice.domain.Invoice;
import java.util.List;
import java.util.UUID;

public interface InvoiceRepository {
    Invoice save(Invoice invoice);
    Invoice findById(UUID id);
    List<Invoice> findByOrderId(UUID orderId);
    boolean existsByInvoiceNumber(String invoiceNumber);
}