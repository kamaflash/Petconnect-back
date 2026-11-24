package com.petconnect.marketplace.presentation.rest;

import com.petconnect.marketplace.application.dto.CreateProductRequest;
import com.petconnect.marketplace.application.dto.ProductResponse;
import com.petconnect.marketplace.application.dto.UpdateProductRequest;
import com.petconnect.marketplace.application.usecases.CreateProductUseCase;
import com.petconnect.marketplace.application.usecases.GetAllProductsUseCase;
import com.petconnect.marketplace.application.usecases.GetProductByIdUseCase;
import com.petconnect.marketplace.application.usecases.UpdateProductUseCase;
import com.petconnect.shared.infrastructure.security.CustomUserDetails;
import com.petconnect.shared.presentation.dto.PageResponse;
import jakarta.validation.Valid;
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
    private final GetProductByIdUseCase getProductByIdUseCase;
    private final UpdateProductUseCase updateProductUseCase;

    public ProductController(
            CreateProductUseCase createProductUseCase,
            GetAllProductsUseCase getAllProductsUseCase,
            GetProductByIdUseCase getProductByIdUseCase,
            UpdateProductUseCase updateProductUseCase) {
        this.createProductUseCase = createProductUseCase;
        this.getAllProductsUseCase = getAllProductsUseCase;
        this.getProductByIdUseCase = getProductByIdUseCase;
        this.updateProductUseCase = updateProductUseCase;
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

    /** Obtener el detalle completo de un producto por su ID. */
    @GetMapping("/products/{id}")
    public ResponseEntity<ProductResponse> getProductById(
            @PathVariable UUID id) {
        return ResponseEntity.ok(getProductByIdUseCase.execute(id));
    }

    /** Actualiza parcialmente un producto. Solo el sellerId dueño puede editarlo. */
    @PutMapping("/products/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProductRequest request) {
        var response = updateProductUseCase.execute(id, userDetails.getUserId(), request);
        return ResponseEntity.ok(response);
    }
}
