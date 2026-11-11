package com.petconnect.invoice.presentation.rest;

import com.petconnect.invoice.application.dto.GenerateInvoiceRequest;
import com.petconnect.invoice.application.dto.InvoiceResponse;
import com.petconnect.invoice.application.usecases.GenerateInvoiceUseCase;
import com.petconnect.invoice.application.usecases.GetInvoicesBySellerUseCase;
import com.petconnect.shared.infrastructure.security.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/invoice-management")
public class InvoiceController {

    private final GenerateInvoiceUseCase generateInvoiceUseCase;
    private final GetInvoicesBySellerUseCase getInvoicesBySellerUseCase;

    public InvoiceController(GenerateInvoiceUseCase generateInvoiceUseCase,
            GetInvoicesBySellerUseCase getInvoicesBySellerUseCase) {
        this.generateInvoiceUseCase = generateInvoiceUseCase;
        this.getInvoicesBySellerUseCase = getInvoicesBySellerUseCase;
    }

    /** Generar una factura para una orden (solo vendedor dueño) */
    @PostMapping("/invoices")
    public ResponseEntity<InvoiceResponse> generateInvoice(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody GenerateInvoiceRequest request) {
        InvoiceResponse invoice = generateInvoiceUseCase.execute(userDetails.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(invoice);
    }

    /** Listar todas las facturas de mi tienda (solo vendedor) */
    @GetMapping("/invoices/seller")
    public ResponseEntity<List<InvoiceResponse>> getMyInvoices(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<InvoiceResponse> invoices = getInvoicesBySellerUseCase.execute(userDetails.getUserId());
        return ResponseEntity.ok(invoices);
    }
}