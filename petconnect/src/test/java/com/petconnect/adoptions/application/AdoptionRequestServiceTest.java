package com.petconnect.adoptions.application;

import com.petconnect.adoptions.domain.AdoptionRequest;
import com.petconnect.adoptions.domain.AdoptionRequestEvent;
import com.petconnect.adoptions.domain.AdoptionStatus;
import com.petconnect.adoptions.domain.repositories.AdoptionRequestRepository;
import com.petconnect.adoptions.domain.repositories.PetOwnershipHistoryRepository;
import com.petconnect.adoptions.infrastructure.persistence.SpringDataAdoptionRequestEventRepository;
import com.petconnect.pets.domain.repositories.PetRepository;
import com.petconnect.shared.infrastructure.websocket.NotificationService;
import com.petconnect.social.domain.repositories.PostRepository;
import com.petconnect.social.infrastructure.persistence.SpringDataNotificationRepository;
import com.petconnect.users.domain.repositories.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdoptionRequestServiceTest {

    private AdoptionRequestRepository adoptionRequestRepository;
    private SpringDataAdoptionRequestEventRepository eventRepository;
    private AdoptionRequestService service;

    @BeforeEach
    void setUp() {
        adoptionRequestRepository = mock(AdoptionRequestRepository.class);
        eventRepository = mock(SpringDataAdoptionRequestEventRepository.class);
        service = new AdoptionRequestService(
                adoptionRequestRepository,
                eventRepository,
                mock(PetOwnershipHistoryRepository.class),
                mock(PetRepository.class),
                mock(UserProfileRepository.class),
                mock(SpringDataNotificationRepository.class),
                mock(PostRepository.class),
                mock(ObjectProvider.class));
    }

    @Test
    void returnsTimelineToAdopterOrPetOwner() {
        UUID requestId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID adopterId = UUID.randomUUID();
        AdoptionRequest request = new AdoptionRequest(
                UUID.randomUUID(), "Nala", null, "CAT", null, ownerId, adopterId, "Adopter", null);
        AdoptionRequestEvent event = new AdoptionRequestEvent(
                requestId, adopterId, AdoptionStatus.PENDING, null);
        event.setId(UUID.randomUUID());
        when(adoptionRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(eventRepository.findByRequestIdOrderByCreatedAtAsc(requestId)).thenReturn(List.of(event));

        var result = service.getRequestTimeline(adopterId, requestId);
        var ownerResult = service.getRequestTimeline(ownerId, requestId);

        assertEquals(1, result.size());
        assertEquals("PENDING", result.get(0).status());
        assertEquals(requestId, result.get(0).requestId());
        assertEquals(1, ownerResult.size());
    }

    @Test
    void deniesTimelineToUnrelatedUser() {
        UUID requestId = UUID.randomUUID();
        AdoptionRequest request = new AdoptionRequest(
                UUID.randomUUID(), "Nala", null, "CAT", null,
                UUID.randomUUID(), UUID.randomUUID(), "Adopter", null);
        when(adoptionRequestRepository.findById(requestId)).thenReturn(Optional.of(request));

        assertThrows(IllegalStateException.class,
                () -> service.getRequestTimeline(UUID.randomUUID(), requestId));
    }
}
