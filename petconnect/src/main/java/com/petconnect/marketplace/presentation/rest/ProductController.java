package com.petconnect.marketplace.presentation.rest;

import com.petconnect.marketplace.application.dto.CreateProductRequest;
import com.petconnect.marketplace.application.dto.ProductResponse;
import com.petconnect.marketplace.application.usecases.CreateProductUseCase;
import com.petconnect.marketplace.application.usecases.GetAllProductsUseCase;
import com.petconnect.shared.infrastructure.security.CustomUserDetails;
import com.petconnect.shared.presentation.dto.PageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/marketplace")
public class ProductController {

    private final CreateProductUseCase createProductUseCase;
    private final GetAllProductsUseCase getAllProductsUseCase;

    public ProductController(
            CreateProductUseCase createProductUseCase,
            GetAllProductsUseCase getAllProductsUseCase) {
        this.createProductUseCase = createProductUseCase;
        this.getAllProductsUseCase = getAllProductsUseCase;
    }

    /**
     * Catálogo paginado (10×10 por defecto, `createdAt` DESC). Cada cambio de
     * filtro en el front llama con `page=0` y los filtros como query params.
     */
    @GetMapping("/products")
    public ResponseEntity<PageResponse<ProductResponse>> getAllProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String term,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean inStock,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(getAllProductsUseCase.execute(
                category, brand, term, minPrice, maxPrice, inStock, page, size));
    }

    @GetMapping("/products/seller")
    public ResponseEntity<PageResponse<ProductResponse>> getMyProducts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(getAllProductsUseCase.executeBySeller(
                userDetails.getUserId(), page, size));
    }

    @GetMapping("/products/category/{category}")
    public ResponseEntity<PageResponse<ProductResponse>> getProductsByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(getAllProductsUseCase.executeByCategory(category, page, size));
    }

    @PostMapping("/products")
    public ResponseEntity<ProductResponse> createProduct(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody CreateProductRequest request) {
        var response = createProductUseCase.execute(userDetails.getUserId(), request);
        return ResponseEntity.ok(response);
    }
}
