package com.petconnect.lostpets.application;

import com.petconnect.lostpets.application.dto.LostPetDtos;
import com.petconnect.lostpets.domain.LostPetReport;
import com.petconnect.lostpets.domain.LostPetReportStatus;
import com.petconnect.lostpets.domain.LostPetSighting;
import com.petconnect.lostpets.domain.repositories.LostPetReportRepository;
import com.petconnect.lostpets.domain.repositories.LostPetSightingRepository;
import com.petconnect.pets.domain.repositories.PetRepository;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Reglas de negocio de avisos de animales perdidos y avistamientos.
 *
 * <p>Permisos (validados siempre en servidor):</p>
 * <ul>
 *   <li>Crear aviso: cualquier usuario autenticado.</li>
 *   <li>Editar / cambiar estado / eliminar aviso: solo el responsable.</li>
 *   <li>Crear avistamiento: cualquier usuario autenticado.</li>
 *   <li>Consultar avisos y avistamientos: público (sin datos personales).</li>
 * </ul>
 */
@Service
public class LostPetService {

    private static final Logger log = LoggerFactory.getLogger(LostPetService.class);

    /** Radio por defecto para notificar a personas cercanas (km). */
    private static final double DEFAULT_NEARBY_RADIUS_KM = 15d;
    private static final int MAX_NEARBY_RECIPIENTS = 50;

    private final LostPetReportRepository reportRepository;
    private final LostPetSightingRepository sightingRepository;
    private final UserProfileRepository userProfileRepository;
    private final PetRepository petRepository;
    private final SpringDataNotificationRepository notificationRepository;
    private final ObjectProvider<NotificationService> notificationServiceProvider;

    public LostPetService(LostPetReportRepository reportRepository,
            LostPetSightingRepository sightingRepository,
            UserProfileRepository userProfileRepository,
            PetRepository petRepository,
            SpringDataNotificationRepository notificationRepository,
            ObjectProvider<NotificationService> notificationServiceProvider) {
        this.reportRepository = reportRepository;
        this.sightingRepository = sightingRepository;
        this.userProfileRepository = userProfileRepository;
        this.petRepository = petRepository;
        this.notificationRepository = notificationRepository;
        this.notificationServiceProvider = notificationServiceProvider;
    }

    // ========== CONSULTAS ==========

    @Transactional(readOnly = true)
    public List<LostPetDtos.LostPetReportResponse> search(UUID viewerId, String species,
            LostPetReportStatus status, LocalDate from, LocalDate to,
            Double latitude, Double longitude, Double radiusKm) {

        List<LostPetReport> reports = reportRepository.findByActiveTrueOrderByCreatedAtDesc();

        if (species != null && !species.isBlank()) {
            reports = reports.stream()
                    .filter(report -> report.getSpecies() != null
                            && report.getSpecies().name().equalsIgnoreCase(species.trim()))
                    .toList();
        }
        if (status != null) {
            reports = reports.stream().filter(report -> report.getStatus() == status).toList();
        }
        if (from != null || to != null) {
            LocalDate effectiveFrom = from != null ? from : LocalDate.of(1970, 1, 1);
            LocalDate effectiveTo = to != null ? to : LocalDate.now().plusYears(5);
            reports = reports.stream()
                    .filter(report -> !report.getLostDate().isBefore(effectiveFrom)
                            && !report.getLostDate().isAfter(effectiveTo))
                    .toList();
        }

        List<LostPetDtos.LostPetReportResponse> responses = reports.stream()
                .map(report -> toResponse(report, viewerId, latitude, longitude))
                .toList();

        if (latitude != null && longitude != null) {
            double radius = resolveRadius(radiusKm);
            responses = responses.stream()
                    .filter(response -> response.distanceKm() != null && response.distanceKm() <= radius)
                    .sorted(Comparator.comparing(LostPetDtos.LostPetReportResponse::distanceKm))
                    .toList();
        }
        return responses;
    }

    @Transactional(readOnly = true)
    public LostPetDtos.LostPetReportResponse getById(UUID viewerId, UUID reportId) {
        return toResponse(requireReport(reportId), viewerId, null, null);
    }

    @Transactional(readOnly = true)
    public List<LostPetDtos.LostPetReportResponse> getMyReports(UUID reporterId) {
        return reportRepository.findByReporterIdOrderByCreatedAtDesc(reporterId).stream()
                .map(report -> toResponse(report, reporterId, null, null))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LostPetDtos.LostPetSightingResponse> getSightings(UUID reportId) {
        requireReport(reportId);
        return sightingRepository.findByReportIdOrderBySightedAtDesc(reportId).stream()
                .map(this::toSightingResponse)
                .toList();
    }

    // ========== AVISOS ==========

    @Transactional
    public LostPetDtos.LostPetReportResponse create(UUID reporterId,
            LostPetDtos.CreateLostPetReport request) {
        validateCommon(request.petName(), request.lostDate(), request.lastSeenArea());

        LostPetReport report = new LostPetReport(
                reporterId,
                request.petId(),
                request.petName().trim(),
                request.species(),
                normalize(request.breed()),
                normalize(request.color()),
                normalize(request.description()),
                normalize(request.photoUrl()),
                request.lostDate(),
                request.lastSeenArea().trim(),
                request.latitude(),
                request.longitude(),
                validateReward(request.rewardAmount()));

        LostPetReport saved = reportRepository.save(report);

        // Si el aviso referencia una mascota propia, se marca como perdida.
        if (saved.getPetId() != null) {
            petRepository.findById(saved.getPetId()).ifPresent(pet -> {
                if (pet.getOwnerId().equals(reporterId) && !pet.isLost()) {
                    pet.markAsLost(saved.getLostDate());
                    pet.setLastKnownLocation(saved.getLastSeenArea());
                    petRepository.save(pet);
                }
            });
        }

        notifyNearby(saved, "LOST_PET_ALERT");
        return toResponse(saved, reporterId, null, null);
    }

    @Transactional
    public LostPetDtos.LostPetReportResponse update(UUID actorId, UUID reportId,
            LostPetDtos.UpdateLostPetReport request) {
        LostPetReport report = requireOwnedReport(actorId, reportId);
        if (report.getStatus().isTerminal()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El aviso está cerrado y ya no se puede editar");
        }
        validateCommon(request.petName(), request.lostDate(), request.lastSeenArea());

        report.updateDetails(
                request.petName().trim(),
                request.species(),
                normalize(request.breed()),
                normalize(request.color()),
                normalize(request.description()),
                normalize(request.photoUrl()),
                request.lostDate(),
                request.lastSeenArea().trim(),
                request.latitude(),
                request.longitude(),
                validateReward(request.rewardAmount()));

        return toResponse(reportRepository.save(report), actorId, null, null);
    }

    @Transactional
    public LostPetDtos.LostPetReportResponse updateStatus(UUID actorId, UUID reportId,
            LostPetDtos.UpdateLostPetStatus request) {
        LostPetReport report = requireOwnedReport(actorId, reportId);
        if (request.status() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado obligatorio");
        }
        try {
            report.changeStatus(request.status());
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage());
        }

        // Al reencontrar (o cerrar) el caso se libera la marca de "perdido".
        if ((request.status() == LostPetReportStatus.REUNITED
                || request.status() == LostPetReportStatus.CLOSED)
                && report.getPetId() != null) {
            petRepository.findById(report.getPetId()).ifPresent(pet -> {
                if (pet.getOwnerId().equals(actorId) && pet.isLost()) {
                    pet.markAsFound();
                    petRepository.save(pet);
                }
            });
        }

        return toResponse(reportRepository.save(report), actorId, null, null);
    }

    @Transactional
    public void delete(UUID actorId, UUID reportId) {
        LostPetReport report = requireOwnedReport(actorId, reportId);
        report.softDelete();
        reportRepository.save(report);
    }

    // ========== AVISTAMIENTOS ==========

    @Transactional
    public LostPetDtos.LostPetSightingResponse addSighting(UUID reporterId, UUID reportId,
            LostPetDtos.CreateLostPetSighting request) {
        LostPetReport report = requireReport(reportId);
        if (request.description() == null || request.description().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La descripción del avistamiento es obligatoria");
        }
        if (report.getStatus().isTerminal()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El aviso está cerrado y no admite nuevos avistamientos");
        }

        LocalDateTime sightedAt = request.sightedAt() != null
                ? request.sightedAt()
                : LocalDateTime.now();
        if (sightedAt.isAfter(LocalDateTime.now().plusMinutes(5))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La fecha del avistamiento no puede ser futura");
        }

        LostPetSighting sighting = new LostPetSighting(
                reportId,
                reporterId,
                sightedAt,
                request.description().trim(),
                normalize(request.area()),
                request.latitude(),
                request.longitude(),
                normalize(request.photoUrl()));

        report.incrementSightings();
        reportRepository.save(report);

        LostPetSighting saved = sightingRepository.save(sighting);
        notifySighting(report, saved);
        return toSightingResponse(saved);
    }

    // ========== NOTIFICACIONES ==========

    private void notifyNearby(LostPetReport report, String type) {
        for (UserProfile profile : nearbyProfiles(report, null)) {
            saveNotification(profile.getAuthUserId(), type,
                    "Nuevo aviso: " + report.getPetName() + " (" + speciesLabel(report)
                            + ") visto por " + report.getLastSeenArea(),
                    report.getReporterId(), report.getId(), "LOST_PET_REPORT");
        }
    }

    private void notifySighting(LostPetReport report, LostPetSighting sighting) {
        saveNotification(report.getReporterId(), "LOST_PET_SIGHTING",
                "Nuevo avistamiento de " + report.getPetName(),
                sighting.getReporterId(), report.getId(), "LOST_PET_REPORT");

        for (UserProfile profile : nearbyProfiles(report, sighting.getReporterId())) {
            saveNotification(profile.getAuthUserId(), "LOST_PET_SIGHTING",
                    "Avistamiento de " + report.getPetName() + " cerca de "
                            + (sighting.getArea() != null
                                    ? sighting.getArea()
                                    : report.getLastSeenArea()),
                    sighting.getReporterId(), report.getId(), "LOST_PET_REPORT");
        }
    }

    /**
     * Personas con coordenadas conocidas, notificaciones activas y dentro del
     * radio del aviso. Excluye al responsable y, si se indica, al autor del
     * avistamiento. La ubicacion nunca se reutiliza para otro fin.
     */
    private List<UserProfile> nearbyProfiles(LostPetReport report, UUID excludedUserId) {
        if (report.getLatitude() == null || report.getLongitude() == null) {
            return List.of();
        }
        return userProfileRepository.findAll().stream()
                .filter(UserProfile::isNotificationsEnabled)
                .filter(profile -> profile.getAuthUserId() != null)
                .filter(profile -> !profile.getAuthUserId().equals(report.getReporterId()))
                .filter(profile -> excludedUserId == null
                        || !profile.getAuthUserId().equals(excludedUserId))
                .filter(profile -> profile.getLatitude() != null && profile.getLongitude() != null)
                .filter(profile -> distanceKm(report.getLatitude(), report.getLongitude(),
                        profile.getLatitude(), profile.getLongitude()) <= DEFAULT_NEARBY_RADIUS_KM)
                .limit(MAX_NEARBY_RECIPIENTS)
                .toList();
    }

    private void saveNotification(UUID userId, String type, String content, UUID actorId,
            UUID referenceId, String referenceType) {
        if (userId == null) {
            return;
        }
        try {
            Notification notification = new Notification(userId, type, content, actorId);
            notification.setReference(referenceId, referenceType);
            notificationRepository.save(notification);

            NotificationService service = notificationServiceProvider.getIfAvailable();
            if (service != null) {
                service.sendToUser(userId, type, content);
            }
        } catch (Exception ex) {
            log.warn("No se pudo registrar la notificacion {} para {}: {}",
                    type, userId, ex.getMessage());
        }
    }


    // ========== UTILIDADES ==========

    private LostPetReport requireReport(UUID reportId) {
        LostPetReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Aviso no encontrado"));
        if (!report.isActive()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Aviso no disponible");
        }
        return report;
    }

    private LostPetReport requireOwnedReport(UUID actorId, UUID reportId) {
        LostPetReport report = requireReport(reportId);
        if (!report.isOwnedBy(actorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo el responsable del aviso puede modificarlo");
        }
        return report;
    }

    private void validateCommon(String petName, LocalDate lostDate, String lastSeenArea) {
        if (petName == null || petName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre es obligatorio");
        }
        if (lostDate == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La fecha de desaparicion es obligatoria");
        }
        if (lostDate.isAfter(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La fecha de desaparicion no puede ser futura");
        }
        if (lastSeenArea == null || lastSeenArea.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Indica la zona aproximada donde se vio por ultima vez");
        }
    }

    private Double validateReward(Double reward) {
        if (reward == null) {
            return null;
        }
        if (reward < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La recompensa no puede ser negativa");
        }
        return reward;
    }

    private double resolveRadius(Double radiusKm) {
        if (radiusKm == null || radiusKm <= 0) {
            return DEFAULT_NEARBY_RADIUS_KM;
        }
        return Math.min(radiusKm, 200d);
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String speciesLabel(LostPetReport report) {
        return report.getSpecies() == null ? "" : report.getSpecies().name().toLowerCase();
    }

    private LostPetDtos.LostPetReportResponse toResponse(LostPetReport report, UUID viewerId,
            Double originLat, Double originLng) {
        UserProfile reporter = userProfileRepository.findByAuthUserId(report.getReporterId())
                .orElse(null);
        Double distance = null;
        if (originLat != null && originLng != null
                && report.getLatitude() != null && report.getLongitude() != null) {
            distance = distanceKm(originLat, originLng,
                    report.getLatitude(), report.getLongitude());
        }
        return new LostPetDtos.LostPetReportResponse(
                report.getId(),
                report.getReporterId(),
                reporter == null ? "Usuario PetConnect" : reporter.getFirstName(),
                report.getPetId(),
                report.getPetName(),
                report.getSpecies() == null ? null : report.getSpecies().name(),
                report.getBreed(),
                report.getColor(),
                report.getDescription(),
                report.getPhotoUrl(),
                report.getLostDate(),
                report.getLastSeenArea(),
                report.getLatitude(),
                report.getLongitude(),
                report.getRewardAmount(),
                report.getStatus(),
                report.getSightingsCount(),
                report.getClosedAt(),
                viewerId != null && report.isOwnedBy(viewerId),
                distance,
                report.getCreatedAt());
    }

    private LostPetDtos.LostPetSightingResponse toSightingResponse(LostPetSighting sighting) {
        UserProfile reporter = userProfileRepository.findByAuthUserId(sighting.getReporterId())
                .orElse(null);
        return new LostPetDtos.LostPetSightingResponse(
                sighting.getId(),
                sighting.getReportId(),
                sighting.getReporterId(),
                reporter == null ? "Usuario PetConnect" : reporter.getFirstName(),
                sighting.getSightedAt(),
                sighting.getDescription(),
                sighting.getArea(),
                sighting.getLatitude(),
                sighting.getLongitude(),
                sighting.getPhotoUrl());
    }

    /** Distancia en km entre dos puntos (formula del semiverseno). */
    static double distanceKm(double lat1, double lng1, double lat2, double lng2) {
        final double earthRadiusKm = 6371d;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double km = earthRadiusKm * c;
        return Math.round(km * 100d) / 100d;
    }

    /** Convierte el filtro de especie recibido por query param. */
    public static Optional<com.petconnect.pets.domain.Species> parseSpecies(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(
                    com.petconnect.pets.domain.Species.valueOf(value.trim().toUpperCase()));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Especie no valida: " + value);
        }
    }
}
