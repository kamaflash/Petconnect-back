package com.petconnect.invoice.application.usecases;

import com.petconnect.invoice.application.dto.InvoiceResponse;
import com.petconnect.invoice.domain.Invoice;
import com.petconnect.invoice.domain.repositories.InvoiceRepository;
import com.petconnect.order.domain.Order;
import com.petconnect.order.domain.repositories.OrderRepository;
import com.petconnect.users.domain.UserProfile;
import com.petconnect.users.domain.repositories.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GetInvoicesBySellerUseCase {

    private final InvoiceRepository invoiceRepository;
    private final OrderRepository orderRepository;
    private final UserProfileRepository userProfileRepository;

    public GetInvoicesBySellerUseCase(InvoiceRepository invoiceRepository,
            OrderRepository orderRepository,
            UserProfileRepository userProfileRepository) {
        this.invoiceRepository = invoiceRepository;
        this.orderRepository = orderRepository;
        this.userProfileRepository = userProfileRepository;
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> execute(UUID sellerId) {
        UserProfile seller = userProfileRepository.findByAuthUserId(sellerId)
                .orElseThrow(() -> new RuntimeException("Seller profile not found"));
        if (!"seller".equalsIgnoreCase(seller.getProfileType())) {
            throw new RuntimeException("This feature is only available to sellers");
        }

        // Obtener todas las órdenes del vendedor y luego sus facturas
        List<Order> orders = orderRepository.findBySellerId(sellerId);
        List<Invoice> invoices = orders.stream()
                .flatMap(order -> invoiceRepository.findByOrderId(order.getId()).stream())
                .sorted((a, b) -> b.getIssueDate().compareTo(a.getIssueDate()))
                .collect(Collectors.toList());

        return invoices.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private InvoiceResponse toResponse(Invoice invoice) {
        return new InvoiceResponse(
                invoice.getId(),
                invoice.getOrderId(),
                invoice.getInvoiceNumber(),
                invoice.getIssueDate(),
                invoice.getTotalAmount(),
                invoice.getTaxAmount(),
                invoice.getStatus(),
                invoice.getPdfUrl(),
                invoice.getCreatedAt(),
                invoice.getUpdatedAt()
        );
    }
}