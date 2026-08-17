package com.petconnect.adoptions.application;

import com.petconnect.adoptions.application.dto.AdoptionHistoryResponse;
import com.petconnect.adoptions.application.dto.AdoptionRequestResponse;
import com.petconnect.adoptions.application.dto.CreateAdoptionRequest;
import com.petconnect.adoptions.application.dto.OwnershipChangeEventResponse;
import com.petconnect.adoptions.application.dto.PetOwnerPeriod;
import com.petconnect.adoptions.domain.AdoptionRequest;
import com.petconnect.adoptions.domain.AdoptionStatus;
import com.petconnect.adoptions.domain.OwnershipChangeReason;
import com.petconnect.adoptions.domain.PetOwnershipHistory;
import com.petconnect.adoptions.domain.repositories.AdoptionRequestRepository;
import com.petconnect.adoptions.domain.repositories.PetOwnershipHistoryRepository;
import com.petconnect.pets.domain.Pet;
import com.petconnect.pets.domain.repositories.PetRepository;
import com.petconnect.shared.infrastructure.websocket.NotificationService;
import com.petconnect.social.domain.Notification;
import com.petconnect.social.infrastructure.persistence.SpringDataNotificationRepository;
import com.petconnect.users.domain.UserProfile;
import com.petconnect.users.domain.repositories.UserProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AdoptionRequestService {

    private static final Logger log = LoggerFactory.getLogger(AdoptionRequestService.class);

    private final AdoptionRequestRepository adoptionRequestRepository;
    private final PetOwnershipHistoryRepository petOwnershipHistoryRepository;
    private final PetRepository petRepository;
    private final UserProfileRepository userProfileRepository;
    private final SpringDataNotificationRepository notificationRepository;
    private final ObjectProvider<NotificationService> notificationServiceProvider;

    public AdoptionRequestService(
            AdoptionRequestRepository adoptionRequestRepository,
            PetOwnershipHistoryRepository petOwnershipHistoryRepository,
            PetRepository petRepository,
            UserProfileRepository userProfileRepository,
            SpringDataNotificationRepository notificationRepository,
            ObjectProvider<NotificationService> notificationServiceProvider) {
        this.adoptionRequestRepository = adoptionRequestRepository;
        this.petOwnershipHistoryRepository = petOwnershipHistoryRepository;
        this.petRepository = petRepository;
        this.userProfileRepository = userProfileRepository;
        this.notificationRepository = notificationRepository;
        this.notificationServiceProvider = notificationServiceProvider;
    }

    @Transactional
    public AdoptionRequestResponse createRequest(UUID adopterId, CreateAdoptionRequest command) {
        if (command.petId() == null) {
            throw new IllegalStateException("petId es obligatorio");
        }

        Pet pet = petRepository.findById(command.petId())
                .orElseThrow(() -> new IllegalArgumentException("Mascota no encontrada"));

        if (!pet.isAvailableForAdoption()) {
            throw new IllegalStateException("La mascota no estÃ¡ disponible para adopciÃ³n");
        }
        if (pet.getOwnerId().equals(adopterId)) {
            throw new IllegalStateException("No puedes solicitar la adopciÃ³n de tu propia mascota");
        }

        // Evitar solicitudes duplicadas: si el usuario ya tiene una solicitud activa
        // (no rechazada ni finalizada) para esta mascota, no puede volver a solicitarla.
        boolean alreadyRequested = adoptionRequestRepository.findByAdopterId(adopterId).stream()
                .anyMatch(r -> r.getPetId().equals(command.petId())
                        && r.getStatus() != AdoptionStatus.REJECTED
                        && r.getStatus() != AdoptionStatus.COMPLETED);
        if (alreadyRequested) {
            throw new IllegalStateException("Ya has solicitado la adopciÃ³n de esta mascota");
        }

        UserProfile adopterProfile = userProfileRepository.findByAuthUserId(adopterId)
                .orElseThrow(() -> new IllegalArgumentException("Perfil del solicitante no encontrado"));
        String adopterName = adopterProfile.getFirstName() + " " + adopterProfile.getLastName();

        AdoptionRequest request = new AdoptionRequest(
                pet.getId(),
                pet.getName(),
                pet.getAvatarUrl(),
                pet.getSpecies().name(),
                pet.getBreed(),
                pet.getOwnerId(),
                adopterId,
                adopterName,
                command.message());

        AdoptionRequest saved = adoptionRequestRepository.save(request);

        // NotificaciÃ³n persistida para el dueÃ±o de la mascota
        String content = String.format("%s ha solicitado adoptar a %s", adopterName, pet.getName());
        Notification notification = new Notification(
                pet.getOwnerId(),
                "ADOPTION_REQUEST",
                content,
                adopterId);
        notificationRepository.save(notification);

        // NotificaciÃ³n por websocket en tiempo real
        NotificationService notificationService = notificationServiceProvider.getIfAvailable();
        if (notificationService != null) {
            try {
                notificationService.notifyAdoptionRequest(pet.getOwnerId(), adopterName, pet.getName());
            } catch (Exception e) {
                log.warn("No se pudo enviar la notificaciÃ³n WebSocket de adopciÃ³n: {}", e.getMessage());
            }
        }

        return AdoptionRequestResponse.from(saved);
    }


    @Transactional(readOnly = true)
    public List<AdoptionRequestResponse> getMyRequests(UUID adopterId) {
        return adoptionRequestRepository.findByAdopterId(adopterId).stream()
                .map(AdoptionRequestResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdoptionRequestResponse> getRequestsForMyPets(UUID ownerId) {
        return adoptionRequestRepository.findByOwnerId(ownerId).stream()
                .map(AdoptionRequestResponse::from)
                .toList();
    }

    /**
     * Devuelve el historial de adopciÃ³n de una mascota: todas las solicitudes
     * de adopciÃ³n registradas y los cambios de propietario de la misma.
     * Se resuelven los nombres de los usuarios implicados para mostrarlos en la UI.
     */
        @Transactional(readOnly = true)
    public AdoptionHistoryResponse getHistory(UUID petId) {
        List<AdoptionRequestResponse> requests = adoptionRequestRepository.findByPetId(petId).stream()
                .map(r -> AdoptionRequestResponse.from(r, resolveUserName(r.getOwnerId())))
                .toList();

        List<OwnershipChangeEventResponse> changes = petOwnershipHistoryRepository.findByPetId(petId).stream()
                .map(c -> OwnershipChangeEventResponse.from(
                        c,
                        resolveUserName(c.getPreviousOwnerId()),
                        resolveUserName(c.getNewOwnerId())))
                .toList();

        List<PetOwnerPeriod> owners = buildOwnersTimeline(petId);

        return AdoptionHistoryResponse.of(petId, requests, changes, owners);
    }

    /**
     * Reconstruye la lÃ­nea de tiempo de propietarios de una mascota a partir
     * de sus cambios de propietario registrados y el propietario actual.
     * <p>
     * Cada {@link PetOwnerPeriod} representa un dueÃ±o y el intervalo de tiempo
     * que lo poseÃ³. El perÃ­odo actual (el dueÃ±o actual de la mascota) tiene
     * {@code toDate} nulo.
     */
    private List<PetOwnerPeriod> buildOwnersTimeline(UUID petId) {
        List<PetOwnerPeriod> periods = new ArrayList<>();
        Pet pet = petRepository.findById(petId).orElse(null);
        if (pet == null) {
            return periods;
        }

        List<PetOwnershipHistory> changes =
                petOwnershipHistoryRepository.findByPetIdOrderByCreatedAtAsc(petId);
        LocalDateTime petCreated = pet.getCreatedAt();

        if (changes.isEmpty()) {
            // Sin cambios de propiedad: el Ãºnico dueÃ±o es el actual
            periods.add(PetOwnerPeriod.of(
                    pet.getOwnerId(),
                    resolveUserName(pet.getOwnerId()),
                    petCreated,
                    null,
                    null,
                    null));
            return periods;
        }

        // Primer propietario: el dueÃ±o que existÃ­a antes del primer cambio
        // (registrado como previousOwnerId del cambio mÃ¡s antiguo)
        PetOwnershipHistory first = changes.get(0);
        periods.add(PetOwnerPeriod.of(
                first.getPreviousOwnerId(),
                resolveUserName(first.getPreviousOwnerId()),
                petCreated,
                first.getCreatedAt(),
                first.getReason().name(),
                first.getAdoptionRequestId()));

        // Propietarios intermedios: el previousOwnerId del cambio i fue
        // dueÃ±o desde el cambio i-1 hasta el cambio i
        for (int i = 1; i < changes.size(); i++) {
            PetOwnershipHistory prevChange = changes.get(i - 1);
            PetOwnershipHistory curChange = changes.get(i);
            periods.add(PetOwnerPeriod.of(
                    curChange.getPreviousOwnerId(),
                    resolveUserName(curChange.getPreviousOwnerId()),
                    prevChange.getCreatedAt(),
                    curChange.getCreatedAt(),
                    curChange.getReason().name(),
                    curChange.getAdoptionRequestId()));
        }

        // Ãšltimo propietario: el newOwnerId del Ãºltimo cambio.
        // Si coincide con el dueÃ±o actual de la mascota, el perÃ­odo estÃ¡ abierto.
        PetOwnershipHistory last = changes.get(changes.size() - 1);
        boolean isCurrentOwner = pet.getOwnerId().equals(last.getNewOwnerId());
        periods.add(PetOwnerPeriod.of(
                last.getNewOwnerId(),
                resolveUserName(last.getNewOwnerId()),
                last.getCreatedAt(),
                isCurrentOwner ? null : last.getCreatedAt(),
                null,
                null));

        return periods;
    }

    /**
     * Resuelve el nombre completo de un usuario a partir de su authUserId.
     * Devuelve null si el usuario no existe (para no romper la respuesta).
     */
    private String resolveUserName(UUID authUserId) {
        if (authUserId == null) {
            return null;
        }
        return userProfileRepository.findByAuthUserId(authUserId)
                .map(p -> p.getFirstName() + " " + p.getLastName())
                .orElse(null);
    }

    @Transactional
    public AdoptionRequestResponse updateStatus(UUID actorId, UUID requestId, String status) {
        AdoptionStatus newStatus;
        try {
            newStatus = AdoptionStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Estado de adopciÃ³n invÃ¡lido: " + status);
        }

        AdoptionRequest request = adoptionRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud de adopciÃ³n no encontrada"));

        if (!request.getOwnerId().equals(actorId)) {
            throw new IllegalStateException("No tienes permisos para modificar esta solicitud");
        }

        AdoptionStatus current = request.getStatus();

        // Rechazar es vÃ¡lido desde cualquier etapa activa (no finalizada)
        if (newStatus == AdoptionStatus.REJECTED) {
            if (current == AdoptionStatus.COMPLETED) {
                throw new IllegalStateException("No se puede rechazar una solicitud finalizada");
            }
        } else {
            // Avanzar etapa: el nuevo estado debe ser el siguiente de la secuencia
            AdoptionStatus expected = nextStage(current);
            if (expected == null || expected != newStatus) {
                throw new IllegalStateException(
                        "No se puede avanzar de " + current.name() + " a " + newStatus.name());
            }
        }

        request.setStatus(newStatus);

        // Al finalizar, transferir la propiedad de la mascota al solicitante
        if (newStatus == AdoptionStatus.COMPLETED) {
            transferPetOwnership(request);
        }

        AdoptionRequest saved = adoptionRequestRepository.save(request);

        // Notificar al solicitante del cambio
        notifyAdoptionChange(request);

        try {
            NotificationService notificationService = notificationServiceProvider.getIfAvailable();
            if (notificationService != null) {
                notificationService.notifyAdoptionUpdate(request.getAdopterId(), request.getPetName(), newStatus.name());
            }
        } catch (Exception e) {
            log.warn("No se pudo enviar la notificaciÃ³n WebSocket de actualizaciÃ³n de adopciÃ³n: {}", e.getMessage());
        }

        return AdoptionRequestResponse.from(saved);
    }

    private AdoptionStatus nextStage(AdoptionStatus current) {
        return switch (current) {
            case PENDING -> AdoptionStatus.PENDING_CONTACT;
            case PENDING_CONTACT -> AdoptionStatus.PENDING_DELIVERY;
            case PENDING_DELIVERY -> AdoptionStatus.COMPLETED;
            case APPROVED -> AdoptionStatus.COMPLETED; // legado: aprobar pasa a finalizar
            default -> null;
        };
    }

    private void transferPetOwnership(AdoptionRequest request) {
        Pet pet = petRepository.findById(request.getPetId())
                .orElseThrow(() -> new IllegalArgumentException("Mascota no encontrada"));
        if (!pet.getOwnerId().equals(request.getOwnerId())) {
            throw new IllegalStateException("La mascota ya fue adoptada por otro usuario");
        }
        pet.setOwnerId(request.getAdopterId());
        pet.setAvailableForAdoption(false);
        pet.setAdoptionDate(LocalDate.now());
        petRepository.save(pet);

        // Registrar el cambio de propietario en el historial
        petOwnershipHistoryRepository.save(new PetOwnershipHistory(
                pet.getId(),
                request.getOwnerId(),
                request.getAdopterId(),
                OwnershipChangeReason.ADOPTION_COMPLETED,
                request.getId()));
    }

    private void notifyAdoptionChange(AdoptionRequest request) {
        String content;
        if (request.getStatus() == AdoptionStatus.COMPLETED) {
            content = String.format("Tu solicitud de adopciÃ³n de %s fue finalizada y la mascota fue entregada al nuevo dueÃ±o", request.getPetName());
        } else if (request.getStatus() == AdoptionStatus.REJECTED) {
            content = String.format("Tu solicitud de adopciÃ³n de %s fue rechazada", request.getPetName());
        } else {
            content = String.format("Tu solicitud de adopciÃ³n de %s avanzÃ³ a %s", request.getPetName(), request.getStatus().name());
        }
        Notification notification = new Notification(
                request.getAdopterId(),
                "ADOPTION_UPDATE",
                content,
                request.getOwnerId());
        notificationRepository.save(notification);
    }
}
