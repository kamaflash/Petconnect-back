package com.petconnect.marketplace.presentation.rest;

import com.petconnect.marketplace.application.dto.CreateProductRequest;
import com.petconnect.marketplace.application.dto.ProductResponse;
import com.petconnect.marketplace.application.usecases.CreateProductUseCase;
import com.petconnect.marketplace.application.usecases.GetAllProductsUseCase;
import com.petconnect.shared.infrastructure.security.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    @GetMapping("/products")
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        return ResponseEntity.ok(getAllProductsUseCase.execute());
    }

    @GetMapping("/products/seller")
    public ResponseEntity<List<ProductResponse>> getMyProducts(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(getAllProductsUseCase.executeBySeller(userDetails.getUserId()));
    }

    @GetMapping("/products/category/{category}")
    public ResponseEntity<List<ProductResponse>> getProductsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(getAllProductsUseCase.executeByCategory(category));
    }

    @PostMapping("/products")
    public ResponseEntity<ProductResponse> createProduct(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody CreateProductRequest request) {
        var response = createProductUseCase.execute(userDetails.getUserId(), request);
        return ResponseEntity.ok(response);
    }
}
