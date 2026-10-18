package com.petconnect.lostpets.application;

import com.petconnect.lostpets.application.dto.LostPetDtos;
import com.petconnect.lostpets.domain.LostPetReport;
import com.petconnect.lostpets.domain.LostPetReportStatus;
import com.petconnect.lostpets.domain.repositories.LostPetReportRepository;
import com.petconnect.lostpets.domain.repositories.LostPetSightingRepository;
import com.petconnect.pets.domain.Species;
import com.petconnect.pets.domain.repositories.PetRepository;
import com.petconnect.social.domain.Notification;
import com.petconnect.social.infrastructure.persistence.SpringDataNotificationRepository;
import com.petconnect.users.domain.UserProfile;
import com.petconnect.users.domain.repositories.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Reglas de negocio y permisos de los avisos de animales perdidos. */
@ExtendWith(MockitoExtension.class)
class LostPetServiceTest {

    @Mock
    private LostPetReportRepository reportRepository;
    @Mock
    private LostPetSightingRepository sightingRepository;
    @Mock
    private UserProfileRepository userProfileRepository;
    @Mock
    private PetRepository petRepository;
    @Mock
    private SpringDataNotificationRepository notificationRepository;

    private LostPetService service;

    private final UUID reporterId = UUID.randomUUID();
    private final UUID strangerId = UUID.randomUUID();
    private final UUID reportId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new LostPetService(reportRepository, sightingRepository, userProfileRepository,
                petRepository, notificationRepository, mock(ObjectProvider.class));
    }

    private LostPetReport report() {
        LostPetReport report = new LostPetReport(reporterId, null, "Rocky", Species.DOG,
                "Bulldog", "Marron", "Lleva collar rojo", null, LocalDate.now().minusDays(3),
                "Parque Central", 40.4168, -3.7038, 100d);
        report.setId(reportId);
        return report;
    }

    private LostPetDtos.CreateLostPetReport createRequest() {
        return new LostPetDtos.CreateLostPetReport(null, "Rocky", Species.DOG, "Bulldog",
                "Marron", "Lleva collar rojo", null, LocalDate.now().minusDays(1),
                "Parque Central", 40.4168, -3.7038, 100d, LostPetReportStatus.LOST);
    }

    private LostPetDtos.UpdateLostPetReport updateRequest() {
        return new LostPetDtos.UpdateLostPetReport("Rocky", Species.DOG, "Bulldog", "Marron",
                "Lleva collar rojo", null, LocalDate.now().minusDays(1), "Parque Central",
                40.4168, -3.7038, 100d);
    }

    // ========== PERMISOS ==========

    @Test
    void onlyReporterCanEditReport() {
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report()));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.update(strangerId, reportId, updateRequest()));

        assertEquals(403, error.getStatusCode().value());
        verify(reportRepository, never()).save(any());
    }

    @Test
    void onlyReporterCanCloseReport() {
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report()));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.updateStatus(strangerId, reportId,
                        new LostPetDtos.UpdateLostPetStatus(LostPetReportStatus.REUNITED)));

        assertEquals(403, error.getStatusCode().value());
    }

    @Test
    void reporterCanMarkAsFoundAndClose() {
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report()));
        when(reportRepository.save(any(LostPetReport.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var found = service.updateStatus(reporterId, reportId,
                new LostPetDtos.UpdateLostPetStatus(LostPetReportStatus.FOUND));
        assertEquals(LostPetReportStatus.FOUND, found.status());

        var closed = service.updateStatus(reporterId, reportId,
                new LostPetDtos.UpdateLostPetStatus(LostPetReportStatus.REUNITED));
        assertEquals(LostPetReportStatus.REUNITED, closed.status());
        assertEquals(LocalDate.now(), closed.closedAt());
    }

    @Test
    void closedReportCannotBeEditedNorReopened() {
        LostPetReport closed = report();
        closed.changeStatus(LostPetReportStatus.CLOSED);
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(closed));

        assertEquals(409, assertThrows(ResponseStatusException.class,
                () -> service.update(reporterId, reportId, updateRequest()))
                .getStatusCode().value());

        assertEquals(409, assertThrows(ResponseStatusException.class,
                () -> service.updateStatus(reporterId, reportId,
                        new LostPetDtos.UpdateLostPetStatus(LostPetReportStatus.FOUND)))
                .getStatusCode().value());

        assertEquals(409, assertThrows(ResponseStatusException.class,
                () -> service.addSighting(strangerId, reportId,
                        new LostPetDtos.CreateLostPetSighting(null, "Lo he visto", null,
                                null, null, null)))
                .getStatusCode().value());
    }

    // ========== AVISTAMIENTOS ==========

    @Test
    void anyAuthenticatedUserCanReportSighting() {
        LostPetReport open = report();
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(open));
        when(reportRepository.save(any(LostPetReport.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(sightingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var created = service.addSighting(strangerId, reportId,
                new LostPetDtos.CreateLostPetSighting(null, "Estaba cerca del quiosco",
                        "Parque Central", 40.417, -3.704, null));

        assertEquals("Estaba cerca del quiosco", created.description());
        assertEquals(strangerId, created.reporterId());
        assertEquals(1, open.getSightingsCount());
    }

    @Test
    void sightingRequiresDescription() {
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report()));

        assertEquals(400, assertThrows(ResponseStatusException.class,
                () -> service.addSighting(strangerId, reportId,
                        new LostPetDtos.CreateLostPetSighting(null, "  ", null, null, null, null)))
                .getStatusCode().value());
    }

    @Test
    void sightingRejectsFutureDates() {
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report()));

        assertEquals(400, assertThrows(ResponseStatusException.class,
                () -> service.addSighting(strangerId, reportId,
                        new LostPetDtos.CreateLostPetSighting(LocalDateTime.now().plusDays(2),
                                "Lo he visto", "Parque", null, null, null)))
                .getStatusCode().value());
    }

    @Test
    void sightingNotifiesReporterAndNearbyUsers() {
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report()));
        when(reportRepository.save(any(LostPetReport.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(sightingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        UserProfile nearby = new UserProfile(UUID.randomUUID(), "Ana", "Lopez", "owner");
        nearby.setNotificationsEnabled(true);
        when(userProfileRepository.findAll()).thenReturn(List.of(nearby));

        service.addSighting(strangerId, reportId,
                new LostPetDtos.CreateLostPetSighting(null, "Lo he visto", "Parque", null,
                        null, null));

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, atLeastOnce()).save(captor.capture());
        assertTrue(captor.getAllValues().stream()
                .anyMatch(n -> "LOST_PET_SIGHTING".equals(n.getType())));
    }

    @Test
    void nearbyNotificationRespectsUserPreferences() {
        when(reportRepository.save(any(LostPetReport.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserProfile muted = new UserProfile(UUID.randomUUID(), "Ana", "Lopez", "owner");
        muted.setNotificationsEnabled(false);
        when(userProfileRepository.findAll()).thenReturn(List.of(muted));

        service.create(reporterId, createRequest());

        verify(notificationRepository, never()).save(any());
    }

    // ========== FILTROS ==========

    @Test
    void searchDelegatesFiltersToDatabaseAndOrdersByNewest() {
        LostPetReport dog = report();
        Page<LostPetReport> page = new PageImpl<>(List.of(dog));
        when(reportRepository.searchActive(any(), any(), any(), any(), any(), any(), anyDouble(),
                any(Pageable.class))).thenReturn(page);

        var result = service.search(null, null, null, null, null, null, null, null, 0, 10);

        assertEquals(1, result.totalElements());
        assertEquals("Rocky", result.content().get(0).petName());

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(reportRepository).searchActive(eq(null), eq(null), eq(null), eq(null),
                eq(null), eq(null), eq(-1d), pageable.capture());
        assertEquals(0, pageable.getValue().getPageNumber());
        assertEquals(10, pageable.getValue().getPageSize());
        assertTrue(pageable.getValue().getSort().getOrderFor("createdAt").isDescending());
    }

    @Test
    void searchSendsProximityFilterToDatabase() {
        when(reportRepository.searchActive(any(), any(), any(), any(), any(), any(), anyDouble(),
                any(Pageable.class))).thenReturn(Page.empty());

        service.search(null, "DOG", LostPetReportStatus.LOST,
                LocalDate.now().minusWeeks(1), LocalDate.now(),
                40.4168, -3.7038, 5d, 1, 10);

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(reportRepository).searchActive(eq(Species.DOG), eq(LostPetReportStatus.LOST),
                eq(LocalDate.now().minusWeeks(1)), eq(LocalDate.now()),
                eq(40.4168), eq(-3.7038), eq(5d), pageable.capture());
        assertEquals(1, pageable.getValue().getPageNumber());
    }

    @Test
    void searchClampsPageSize() {
        when(reportRepository.searchActive(any(), any(), any(), any(), any(), any(), anyDouble(),
                any(Pageable.class))).thenReturn(Page.empty());

        service.search(null, null, null, null, null, null, null, null, 0, 500);

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(reportRepository).searchActive(any(), any(), any(), any(), any(), any(), anyDouble(),
                pageable.capture());
        assertEquals(50, pageable.getValue().getPageSize());
    }

    // ========== VALIDACIONES Y PRIVACIDAD ==========

    @Test
    void createRejectsFutureLostDate() {
        assertEquals(400, assertThrows(ResponseStatusException.class,
                () -> service.create(reporterId, new LostPetDtos.CreateLostPetReport(null, "Rocky",
                        Species.DOG, null, null, null, null, LocalDate.now().plusDays(1), "Parque",
                        null, null, null, LostPetReportStatus.LOST)))
                .getStatusCode().value());
    }

    @Test
    void createRejectsNegativeReward() {
        assertEquals(400, assertThrows(ResponseStatusException.class,
                () -> service.create(reporterId, new LostPetDtos.CreateLostPetReport(null, "Rocky",
                        Species.DOG, null, null, null, null, LocalDate.now(), "Parque", null, null,
                        -5d, LostPetReportStatus.LOST)))
                .getStatusCode().value());
    }

    @Test
    void createRequiresApproximateArea() {
        assertEquals(400, assertThrows(ResponseStatusException.class,
                () -> service.create(reporterId, new LostPetDtos.CreateLostPetReport(null, "Rocky",
                        Species.DOG, null, null, null, null, LocalDate.now(), "   ", null, null,
                        null, LostPetReportStatus.LOST)))
                .getStatusCode().value());
    }

    @Test
    void responseNeverExposesPersonalContactData() {
        UserProfile reporter = new UserProfile(reporterId, "Juan", "Perez", "owner");
        reporter.updateProfile("Juan", "Perez", "+34600123456", "bio", null);
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report()));
        when(userProfileRepository.findByAuthUserId(reporterId))
                .thenReturn(Optional.of(reporter));

        var response = service.getById(strangerId, reportId);

        assertEquals("Juan", response.reporterName());
        assertFalse(response.owner());
    }

    @Test
    void speciesFilterRejectsUnknownValues() {
        assertEquals(400, assertThrows(ResponseStatusException.class,
                () -> LostPetService.parseSpecies("DRAGON")).getStatusCode().value());
        assertTrue(LostPetService.parseSpecies(null).isEmpty());
    }
}
