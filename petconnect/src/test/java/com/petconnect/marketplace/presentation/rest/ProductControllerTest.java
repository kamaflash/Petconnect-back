package com.petconnect.marketplace.presentation.rest;

import com.petconnect.marketplace.application.dto.ProductResponse;
import com.petconnect.marketplace.application.dto.UpdateProductRequest;
import com.petconnect.marketplace.application.usecases.UpdateProductUseCase;
import com.petconnect.shared.infrastructure.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private UpdateProductUseCase updateProductUseCase;

    private ProductController controller;

    private UUID productId;
    private UUID sellerId;
    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        controller = new ProductController(
                null, null, null, updateProductUseCase);
        productId = UUID.randomUUID();
        sellerId = UUID.randomUUID();
        userDetails = new CustomUserDetails(
                sellerId, "seller@test.com", "password", true, List.of());
    }

    @Test
    void updateProduct_returnsOkWithUpdatedProduct() {
        var expected = new ProductResponse(
                productId, sellerId, "Alimento Premium", "Descripcion",
                new BigDecimal("45.90"), new BigDecimal("52.00"),
                "Alimento", "Perros", "https://img.example.com/pet.jpg",
                List.of("https://img.example.com/pet.jpg"),
                100, "PetNutri", BigDecimal.ZERO, 0,
                true, null, "perro,alimento",
                LocalDateTime.now(), LocalDateTime.now()
        );
        var request = new UpdateProductRequest(
                "Alimento Premium", null, null, null, null, null,
                null, null, null, null, null, null, null);

        when(updateProductUseCase.execute(productId, sellerId, request))
                .thenReturn(expected);

        ResponseEntity<ProductResponse> response = controller.updateProduct(userDetails, productId, request);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(productId, response.getBody().id());
        assertEquals("Alimento Premium", response.getBody().name());
        assertEquals(sellerId, response.getBody().sellerId());
        assertTrue(response.getBody().active());
        verify(updateProductUseCase).execute(productId, sellerId, request);
    }
}