package com.petconnect.marketplace.application.usecases;

import com.petconnect.marketplace.application.dto.ProductResponse;
import com.petconnect.marketplace.application.dto.UpdateProductRequest;
import com.petconnect.marketplace.domain.Product;
import com.petconnect.marketplace.domain.exceptions.ProductNotFoundException;
import com.petconnect.marketplace.domain.exceptions.ProductNotOwnedException;
import com.petconnect.marketplace.domain.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateProductUseCaseTest {

    @Mock
    private ProductRepository productRepository;

    private UpdateProductUseCase useCase;

    private UUID productId;
    private UUID sellerId;
    private UUID otherSellerId;
    private Product product;

    @BeforeEach
    void setUp() {
        useCase = new UpdateProductUseCase(productRepository);
        productId = UUID.randomUUID();
        sellerId = UUID.randomUUID();
        otherSellerId = UUID.randomUUID();
        product = new Product(
                sellerId,
                "Alimento Premium",
                "Saco de 15 kg",
                new BigDecimal("45.90"),
                "Alimento",
                "Perros",
                "https://images.example.com/pet1.jpg",
                100,
                "PetNutri",
                "perro,alimento"
        );
        // Forzar id y timestamps para tests deterministas
        product.setId(productId);
    }

    @Test
    void ownerUpdatesPartially_nameAndPrice_returnsUpdatedProduct() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        var request = new UpdateProductRequest(
                "Alimento Premium Adulto", null, new BigDecimal("52.00"), null,
                null, null, null, null, 80, null, null, null, null);

        var response = useCase.execute(productId, sellerId, request);

        assertEquals(productId, response.id());
        assertEquals("Alimento Premium Adulto", response.name());
        assertEquals(new BigDecimal("52.00"), response.price());
        assertEquals(80, response.stock());
        // Campos no enviados se conservan
        assertEquals("Saco de 15 kg", response.description());
        assertEquals("Alimento", response.category());
        assertEquals(true, response.active());
        verify(productRepository).save(product);
    }

    @Test
    void ownerUpdatesActive_false_productIsDeactivated() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        var request = new UpdateProductRequest(
                null, null, null, null, null, null, null, null, null, null, null, null, false);

        var response = useCase.execute(productId, sellerId, request);

        assertFalse(response.active());
    }

    @Test
    void nonOwnerTriesToUpdate_throwsProductNotOwnedException() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        var request = new UpdateProductRequest(
                "Otro nombre", null, null, null, null, null, null, null, null, null, null, null, null);

        assertThrows(ProductNotOwnedException.class,
                () -> useCase.execute(productId, otherSellerId, request));
        verify(productRepository, never()).save(any());
    }

    @Test
    void productNotFound_throwsProductNotFoundException() {
        UUID notFoundId = UUID.randomUUID();
        when(productRepository.findById(notFoundId)).thenReturn(Optional.empty());

        var request = new UpdateProductRequest(
                "Nombre", null, null, null, null, null, null, null, null, null, null, null, null);

        assertThrows(ProductNotFoundException.class,
                () -> useCase.execute(notFoundId, sellerId, request));
    }
}