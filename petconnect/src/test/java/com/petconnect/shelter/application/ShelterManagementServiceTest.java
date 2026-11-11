package com.petconnect.shelter.application;

import com.petconnect.shelter.application.dto.ShelterVisitVisitorResponse;
import com.petconnect.shelter.application.dto.ShelterDonationLinkRequest;
import com.petconnect.shelter.domain.ShelterVisitRequest;
import com.petconnect.shelter.domain.ShelterVisitStatus;
import com.petconnect.shelter.infrastructure.persistence.ShelterVisitRequestJpaRepository;
import com.petconnect.shared.infrastructure.websocket.NotificationService;
import com.petconnect.social.infrastructure.persistence.SpringDataNotificationRepository;
import com.petconnect.users.domain.UserProfile;
import com.petconnect.users.domain.repositories.UserProfileRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShelterManagementServiceTest {

    private ShelterVisitRequestJpaRepository visitRepository;
    private UserProfileRepository profileRepository;
    private ShelterManagementService service;

    @BeforeEach
    void setUp() {
        visitRepository = mock(ShelterVisitRequestJpaRepository.class);
        profileRepository = mock(UserProfileRepository.class);
        service = new ShelterManagementService(
                visitRepository, profileRepository, mock(SpringDataNotificationRepository.class),
                mock(ObjectProvider.class));
    }

    @Test
    void visitorAcceptingAlternativeDateConfirmsTheProposedTime() {
        UUID shelterId = UUID.randomUUID();
        UUID visitorId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();
        LocalDateTime proposedTime = LocalDateTime.now().plusDays(3);
        ShelterVisitRequest request = new ShelterVisitRequest(
                shelterId, visitorId, "Visitor", LocalDateTime.now().plusDays(2), 2, null);
        request.setId(requestId);
        request.updateStatus(ShelterVisitStatus.ALTERNATIVE_PROPOSED, "Please come later", proposedTime);
        when(visitRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(visitRepository.save(request)).thenReturn(request);

        var result = service.respondToVisit(visitorId, requestId,
                new ShelterVisitVisitorResponse(ShelterVisitStatus.CONFIRMED));

        assertEquals(ShelterVisitStatus.CONFIRMED, result.status());
        assertEquals(proposedTime, result.visitAt());
        assertEquals(null, result.proposedVisitAt());
    }

    @Test
    void visitorCannotRespondToAnotherUsersRequest() {
        UUID requestId = UUID.randomUUID();
        ShelterVisitRequest request = new ShelterVisitRequest(
                UUID.randomUUID(), UUID.randomUUID(), "Visitor", LocalDateTime.now().plusDays(2), 1, null);
        when(visitRepository.findById(requestId)).thenReturn(Optional.of(request));

        assertThrows(ResponseStatusException.class, () -> service.respondToVisit(
                UUID.randomUUID(), requestId, new ShelterVisitVisitorResponse(ShelterVisitStatus.CANCELLED)));
    }

    @Test
    void shelterCanUpdateAndClearItsDonationLink() {
        UUID shelterId = UUID.randomUUID();
        UserProfile shelter = new UserProfile(shelterId, "Shelter", "Name", "shelter");
        when(profileRepository.findByAuthUserId(shelterId)).thenReturn(Optional.of(shelter));
        when(profileRepository.save(shelter)).thenReturn(shelter);

        var saved = service.updateDonationLink(shelterId,
                new ShelterDonationLinkRequest(" https://donate.example.org/shelter "));
        assertEquals("https://donate.example.org/shelter", saved.donationUrl());

        var cleared = service.updateDonationLink(shelterId, new ShelterDonationLinkRequest(""));
        assertEquals(null, cleared.donationUrl());
    }

    @Test
    void donationLinkMustUseHttps() {
        UUID shelterId = UUID.randomUUID();
        UserProfile shelter = new UserProfile(shelterId, "Shelter", "Name", "shelter");
        when(profileRepository.findByAuthUserId(shelterId)).thenReturn(Optional.of(shelter));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.updateDonationLink(shelterId,
                        new ShelterDonationLinkRequest("http://donate.example.org")));

        assertEquals(400, error.getStatusCode().value());
    }

    @Test
    void nonShelterCannotSetDonationLink() {
        UUID userId = UUID.randomUUID();
        UserProfile user = new UserProfile(userId, "Regular", "User", "user");
        when(profileRepository.findByAuthUserId(userId)).thenReturn(Optional.of(user));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.updateDonationLink(userId,
                        new ShelterDonationLinkRequest("https://donate.example.org")));

        assertEquals(403, error.getStatusCode().value());
    }
}
