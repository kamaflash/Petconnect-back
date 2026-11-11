package com.petconnect.invoice.application.usecases;

import com.petconnect.invoice.application.dto.GenerateInvoiceRequest;
import com.petconnect.invoice.application.dto.InvoiceResponse;
import com.petconnect.invoice.domain.Invoice;
import com.petconnect.invoice.domain.repositories.InvoiceRepository;
import com.petconnect.order.domain.Order;
import com.petconnect.order.domain.repositories.OrderRepository;
import com.petconnect.users.domain.UserProfile;
import com.petconnect.users.domain.repositories.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class GenerateInvoiceUseCase {

    private final InvoiceRepository invoiceRepository;
    private final OrderRepository orderRepository;
    private final UserProfileRepository userProfileRepository;

    public GenerateInvoiceUseCase(InvoiceRepository invoiceRepository,
            OrderRepository orderRepository,
            UserProfileRepository userProfileRepository) {
        this.invoiceRepository = invoiceRepository;
        this.orderRepository = orderRepository;
        this.userProfileRepository = userProfileRepository;
    }

    @Transactional
    public InvoiceResponse execute(UUID sellerId, GenerateInvoiceRequest request) {
        // Validar que el vendedor exista y sea tipo seller
        UserProfile seller = userProfileRepository.findByAuthUserId(sellerId)
                .orElseThrow(() -> new RuntimeException("Seller profile not found"));
        if (!"seller".equalsIgnoreCase(seller.getProfileType())) {
            throw new RuntimeException("This feature is only available to sellers");
        }

        Order order = orderRepository.findById(request.orderId());
        if (!order.getSellerId().equals(sellerId)) {
            throw new RuntimeException("You are not the owner of this order");
        }

        // Generar número de factura
        String invoiceNumber = "INV-" + order.getId().toString().substring(0, 8).toUpperCase();

        // Verificar que no exista ya una factura para esta orden
        if (invoiceRepository.findByOrderId(order.getId()).stream()
                .anyMatch(inv -> !"ANNULLED".equals(inv.getStatus()))) {
            throw new RuntimeException("An invoice already exists for this order");
        }

        BigDecimal tax = request.taxAmount() != null ? request.taxAmount() : BigDecimal.ZERO;
        BigDecimal total = order.getTotalAmount().add(tax);

        Invoice invoice = new Invoice(
                order.getId(),
                invoiceNumber,
                LocalDateTime.now(),
                total,
                tax,
                "EMITED"
        );

        Invoice saved = invoiceRepository.save(invoice);

        return new InvoiceResponse(
                saved.getId(),
                saved.getOrderId(),
                saved.getInvoiceNumber(),
                saved.getIssueDate(),
                saved.getTotalAmount(),
                saved.getTaxAmount(),
                saved.getStatus(),
                saved.getPdfUrl(),
                saved.getCreatedAt(),
                saved.getUpdatedAt()
        );
    }
}