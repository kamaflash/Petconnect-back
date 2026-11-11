package com.petconnect.invoice.domain;

import com.petconnect.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "invoices")
public class Invoice extends BaseEntity {

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "invoice_number", nullable = false, length = 50, unique = true)
    private String invoiceNumber;

    @Column(name = "issue_date", nullable = false)
    private LocalDateTime issueDate;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "tax_amount", precision = 12, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(nullable = false, length = 20)
    private String status = "EMITED";

    @Column(name = "pdf_url", length = 500)
    private String pdfUrl;

    protected Invoice() {
        super();
    }

    public Invoice(UUID orderId, String invoiceNumber, LocalDateTime issueDate,
            BigDecimal totalAmount, BigDecimal taxAmount, String status) {
        super();
        this.orderId = orderId;
        this.invoiceNumber = invoiceNumber;
        this.issueDate = issueDate;
        this.totalAmount = totalAmount;
        this.taxAmount = taxAmount;
        this.status = status;
    }

    public void setPdfUrl(String pdfUrl) {
        this.pdfUrl = pdfUrl;
    }

    public void updateStatus(String status) {
        this.status = status;
    }

    public UUID getOrderId() { return orderId; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public LocalDateTime getIssueDate() { return issueDate; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public BigDecimal getTaxAmount() { return taxAmount; }
    public String getStatus() { return status; }
    public String getPdfUrl() { return pdfUrl; }
}