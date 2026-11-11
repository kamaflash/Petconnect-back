package com.petconnect.shelter.application;

import com.petconnect.shelter.application.dto.ShelterDonationLinkRequest;
import com.petconnect.shelter.application.dto.ShelterDonationLinkResponse;
import com.petconnect.shelter.application.dto.ShelterVisitRequestCreate;
import com.petconnect.shelter.application.dto.ShelterVisitRequestResponse;
import com.petconnect.shelter.application.dto.ShelterVisitStatusUpdate;
import com.petconnect.shelter.application.dto.ShelterVisitVisitorResponse;
import com.petconnect.shelter.domain.ShelterVisitRequest;
import com.petconnect.shelter.domain.ShelterVisitStatus;
import com.petconnect.shelter.infrastructure.persistence.ShelterVisitRequestJpaRepository;
import com.petconnect.shared.infrastructure.websocket.NotificationService;
import com.petconnect.social.domain.Notification;
import com.petconnect.social.infrastructure.persistence.SpringDataNotificationRepository;
import com.petconnect.users.domain.UserProfile;
import com.petconnect.users.domain.repositories.UserProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@Service
public class ShelterManagementService {

    private static final Logger log = LoggerFactory.getLogger(ShelterManagementService.class);

    private final ShelterVisitRequestJpaRepository visitRepository;
    private final UserProfileRepository profileRepository;
    private final SpringDataNotificationRepository notificationRepository;
    private final ObjectProvider<NotificationService> notificationServiceProvider;

    public ShelterManagementService(
            ShelterVisitRequestJpaRepository visitRepository,
            UserProfileRepository profileRepository,
            SpringDataNotificationRepository notificationRepository,
            ObjectProvider<NotificationService> notificationServiceProvider) {
        this.visitRepository = visitRepository;
        this.profileRepository = profileRepository;
        this.notificationRepository = notificationRepository;
        this.notificationServiceProvider = notificationServiceProvider;
    }

    @Transactional(readOnly = true)
    public ShelterDonationLinkResponse getPublicDonationLink(UUID shelterUserId) {
        return new ShelterDonationLinkResponse(requireShelter(shelterUserId).getDonationUrl());
    }

    @Transactional
    public ShelterDonationLinkResponse updateDonationLink(UUID actorId, ShelterDonationLinkRequest request) {
        UserProfile shelter = requireShelter(actorId);
        String donationUrl = normalizeOptional(request.donationUrl());
        if (donationUrl != null) {
            try {
                URI uri = URI.create(donationUrl);
                if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null || uri.getUserInfo() != null) {
                    throw new IllegalArgumentException("Invalid donation URL");
                }
            } catch (IllegalArgumentException exception) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Donation URL must be a valid HTTPS link");
            }
            if (donationUrl.length() > 2048) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Donation URL exceeds 2048 characters");
            }
        }
        shelter.updateDonationUrl(donationUrl);
        UserProfile saved = profileRepository.save(shelter);
        return new ShelterDonationLinkResponse(saved.getDonationUrl());
    }

    @Transactional
    public ShelterVisitRequestResponse requestVisit(UUID visitorId, ShelterVisitRequestCreate request) {
        UserProfile shelter = requireShelter(request.shelterUserId());
        if (visitorId.equals(request.shelterUserId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A shelter cannot request a visit to itself");
        }
        if (!request.visitAt().isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Visit date must be in the future");
        }
        if (visitRepository.existsByShelterUserIdAndVisitorUserIdAndStatusIn(
                shelter.getAuthUserId(), visitorId,
                List.of(ShelterVisitStatus.PENDING, ShelterVisitStatus.CONFIRMED, ShelterVisitStatus.ALTERNATIVE_PROPOSED))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An active visit request already exists");
        }
        UserProfile visitor = profileRepository.findByAuthUserId(visitorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Visitor profile not found"));
        String visitorName = (visitor.getFirstName() + " " + visitor.getLastName()).trim();
        ShelterVisitRequest visit = new ShelterVisitRequest(
                shelter.getAuthUserId(), visitorId, visitorName, request.visitAt(), request.partySize(),
                normalizeOptional(request.message()));
        ShelterVisitRequest saved = visitRepository.save(visit);
        notifyUser(shelter.getAuthUserId(), "SHELTER_VISIT_REQUEST",
                visitorName + " solicita visitar la protectora el " + request.visitAt(),
                visitorId, saved, "SHELTER_VISIT_REQUEST");
        return ShelterVisitRequestResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<ShelterVisitRequestResponse> getMyVisitRequests(UUID shelterUserId) {
        requireShelter(shelterUserId);
        return visitRepository.findByShelterUserIdOrderByVisitAtAsc(shelterUserId)
                .stream().map(ShelterVisitRequestResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ShelterVisitRequestResponse> getMyVisitorRequests(UUID shelterUserId, UUID visitorId) {
        requireShelter(shelterUserId);
        return visitRepository.findByShelterUserIdAndVisitorUserIdOrderByVisitAtAsc(shelterUserId, visitorId)
                .stream().map(ShelterVisitRequestResponse::from).toList();
    }

    @Transactional
    public ShelterVisitRequestResponse updateVisitStatus(
            UUID shelterUserId, UUID requestId, ShelterVisitStatusUpdate update) {
        requireShelter(shelterUserId);
        ShelterVisitRequest visit = visitRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Visit request not found"));
        if (!shelterUserId.equals(visit.getShelterUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Visit request belongs to another shelter");
        }
        validateVisitTransition(visit.getStatus(), update.status(), update.responseNote(), update.proposedVisitAt());
        visit.updateStatus(update.status(), normalizeOptional(update.responseNote()), update.proposedVisitAt());
        ShelterVisitRequest saved = visitRepository.save(visit);
        String content = update.status() == ShelterVisitStatus.ALTERNATIVE_PROPOSED
                ? "La protectora propone otro horario para tu visita: " + update.proposedVisitAt()
                : "La protectora ha actualizado tu solicitud de visita: " + update.status().name().toLowerCase();
        notifyUser(saved.getVisitorUserId(), "SHELTER_VISIT_UPDATE", content,
                shelterUserId, saved, "SHELTER_VISIT_UPDATE");
        return ShelterVisitRequestResponse.from(saved);
    }

    @Transactional
    public ShelterVisitRequestResponse respondToVisit(UUID visitorId, UUID requestId, ShelterVisitVisitorResponse response) {
        ShelterVisitRequest visit = visitRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Visit request not found"));
        if (!visitorId.equals(visit.getVisitorUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Visit request belongs to another visitor");
        }
        if (response.status() == ShelterVisitStatus.CONFIRMED
                && visit.getStatus() == ShelterVisitStatus.ALTERNATIVE_PROPOSED) {
            visit.updateStatus(ShelterVisitStatus.CONFIRMED, null, null);
        } else if (response.status() == ShelterVisitStatus.CANCELLED
                && (visit.getStatus() == ShelterVisitStatus.PENDING
                        || visit.getStatus() == ShelterVisitStatus.CONFIRMED
                        || visit.getStatus() == ShelterVisitStatus.ALTERNATIVE_PROPOSED)) {
            visit.updateStatus(ShelterVisitStatus.CANCELLED, null, null);
        } else {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Invalid visitor response");
        }
        ShelterVisitRequest saved = visitRepository.save(visit);
        notifyUser(saved.getShelterUserId(), "SHELTER_VISIT_REQUEST",
                saved.getVisitorName() + (response.status() == ShelterVisitStatus.CONFIRMED
                        ? " ha aceptado el horario alternativo de visita."
                        : " ha cancelado la solicitud de visita."),
                visitorId, saved, "SHELTER_VISIT_REQUEST");
        return ShelterVisitRequestResponse.from(saved);
    }

    private UserProfile requireShelter(UUID authUserId) {
        UserProfile profile = profileRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shelter profile not found"));
        if (!"shelter".equalsIgnoreCase(profile.getProfileType())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This feature is only available to shelters");
        }
        return profile;
    }

    private void validateVisitTransition(
            ShelterVisitStatus current, ShelterVisitStatus next, String responseNote, LocalDateTime proposedVisitAt) {
        boolean valid = switch (current) {
            case PENDING -> next == ShelterVisitStatus.CONFIRMED
                    || next == ShelterVisitStatus.ALTERNATIVE_PROPOSED
                    || next == ShelterVisitStatus.REJECTED;
            case CONFIRMED, ALTERNATIVE_PROPOSED ->
                    next == ShelterVisitStatus.ALTERNATIVE_PROPOSED || next == ShelterVisitStatus.COMPLETED;
            case REJECTED, COMPLETED, CANCELLED -> false;
        };
        if (!valid) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Invalid visit request status transition");
        }
        if (next == ShelterVisitStatus.ALTERNATIVE_PROPOSED
                && (proposedVisitAt == null || !proposedVisitAt.isAfter(LocalDateTime.now()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A future alternative visit time is required");
        }
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private void notifyUser(
            UUID recipientId, String type, String content, UUID actorId, ShelterVisitRequest request, String referenceType) {
        Notification notification = new Notification(recipientId, type, content, actorId);
        notification.setReference(request.getId(), referenceType);
        notificationRepository.save(notification);

        NotificationService notificationService = notificationServiceProvider.getIfAvailable();
        if (notificationService != null) {
            try {
                notificationService.sendToUser(recipientId, type, notification);
            } catch (RuntimeException exception) {
                log.warn("Could not send shelter visit WebSocket notification to user {}", recipientId, exception);
            }
        }
    }
}
