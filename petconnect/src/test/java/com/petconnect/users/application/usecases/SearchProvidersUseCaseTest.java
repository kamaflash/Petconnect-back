package com.petconnect.users.application.usecases;

import com.petconnect.users.application.dto.ProviderSearchResponse;
import com.petconnect.users.domain.UserProfile;
import com.petconnect.users.domain.repositories.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** La búsqueda de proveedores pagina, normaliza filtros y mapea perfiles. */
@ExtendWith(MockitoExtension.class)
class SearchProvidersUseCaseTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    private SearchProvidersUseCase useCase;

    private final UUID authUserId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new SearchProvidersUseCase(userProfileRepository);
    }

    @Test
    @SuppressWarnings("unchecked")
    void vetTypeWithServicesReturnsMappedPage() {
        var vet = new UserProfile(authUserId, "Ana", "García", "vet");
        vet.updateServices("consultations,vaccinations");
        var page = new PageImpl<>(List.of(vet));

        when(userProfileRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);

        ProviderSearchResponse response = useCase.execute("vet", List.of("Consultations", " vaccinations "), 0, 10);

        assertEquals(1, response.totalElements());
        assertEquals(0, response.page());
        assertEquals(1, response.size());
        assertEquals(1, response.content().size());
        assertEquals("consultations,vaccinations", response.content().get(0).services());

        var specCaptor = ArgumentCaptor.forClass(Specification.class);
        var pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(userProfileRepository).findAll(specCaptor.capture(), pageableCaptor.capture());
        assertEquals(0, pageableCaptor.getValue().getPageNumber());
        assertEquals(10, pageableCaptor.getValue().getPageSize());
    }

    @Test
    @SuppressWarnings("unchecked")
    void unknownTypeFallsBackToAllProviders() {
        var page = new PageImpl<UserProfile>(List.of());

        when(userProfileRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);

        ProviderSearchResponse response = useCase.execute("admin", null, 0, 10);

        assertEquals(0, response.totalElements());
        verify(userProfileRepository).findAll(any(Specification.class), any(Pageable.class));
    }
}
