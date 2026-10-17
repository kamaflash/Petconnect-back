package com.petconnect.social.application;

import com.petconnect.social.application.dto.CommunityDtos;
import com.petconnect.social.domain.CommunityGroup;
import com.petconnect.social.domain.CommunityGroupMember;
import com.petconnect.social.infrastructure.persistence.SpringDataCommunityGroupMemberRepository;
import com.petconnect.social.infrastructure.persistence.SpringDataGroupRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Reglas de pertenencia de los circulos de comunidad. */
@ExtendWith(MockitoExtension.class)
class CommunityServiceTest {

    @Mock
    private SpringDataGroupRepository groupRepository;

    @Mock
    private SpringDataCommunityGroupMemberRepository memberRepository;

    private final UUID userId = UUID.randomUUID();
    private final UUID groupId = UUID.randomUUID();

    private CommunityService newService() {
        return new CommunityService(groupRepository, memberRepository);
    }

    private CommunityGroup group() {
        CommunityGroup created = new CommunityGroup("Paseos en Retiro", "Quedadas para pasear",
                "https://example.com/a.png", "LOCATION");
        // BaseEntity.setId es publico: sin id el servicio no puede resolver la pertenencia.
        created.setId(groupId);
        return created;
    }

    /** Circulo con propietario, como los que genera CommunityService.create. */
    private CommunityGroup ownedGroup() {
        CommunityGroup created = group();
        created.setCreatedBy(userId);
        return created;
    }

    private CommunityDtos.CreateCommunityRequest createRequest(String name, String topic) {
        return new CommunityDtos.CreateCommunityRequest(name, "Quedadas para pasear",
                "https://example.com/a.png", "LOCATION", topic);
    }

    private CommunityDtos.UpdateCommunityRequest updateRequest(String name, String topic) {
        return new CommunityDtos.UpdateCommunityRequest(name, "Quedadas para pasear",
                "https://example.com/a.png", "LOCATION", topic);
    }

    @Test
    void joinCreatesMembershipAndIncrementsCount() {
        CommunityGroup group = group();
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(memberRepository.existsByGroupIdAndUserId(groupId, userId)).thenReturn(false);
        when(memberRepository.save(any(CommunityGroupMember.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(groupRepository.save(group)).thenReturn(group);
        when(memberRepository.findByGroupIdAndUserId(groupId, userId)).thenReturn(Optional
                .of(new CommunityGroupMember(groupId, userId, CommunityGroupMember.Role.MEMBER)));

        CommunityDtos.CommunityResponse response = newService().join(userId, groupId);

        assertTrue(response.member());
        assertEquals(1, response.membersCount());
        verify(memberRepository).save(any(CommunityGroupMember.class));
    }

    @Test
    void joinIsIdempotentAndDoesNotDoubleCount() {
        CommunityGroup group = group();
        group.incrementMembers();
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(memberRepository.existsByGroupIdAndUserId(groupId, userId)).thenReturn(true);
        when(memberRepository.findByGroupIdAndUserId(groupId, userId)).thenReturn(Optional
                .of(new CommunityGroupMember(groupId, userId, CommunityGroupMember.Role.MEMBER)));

        CommunityDtos.CommunityResponse response = newService().join(userId, groupId);

        assertTrue(response.member());
        assertEquals(1, response.membersCount());
        verify(memberRepository, never()).save(any(CommunityGroupMember.class));
    }

    @Test
    void leaveRemovesMembershipAndDecrementsCount() {
        CommunityGroup group = group();
        group.incrementMembers();
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(memberRepository.existsByGroupIdAndUserId(groupId, userId)).thenReturn(true);
        when(groupRepository.save(group)).thenReturn(group);
        when(memberRepository.findByGroupIdAndUserId(groupId, userId)).thenReturn(Optional.empty());

        CommunityDtos.CommunityResponse response = newService().leave(userId, groupId);

        assertFalse(response.member());
        assertEquals(0, response.membersCount());
        verify(memberRepository).deleteByGroupIdAndUserId(groupId, userId);
    }

    @Test
    void leaveIsIdempotentForNonMembers() {
        CommunityGroup group = group();
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(memberRepository.existsByGroupIdAndUserId(groupId, userId)).thenReturn(false);
        when(memberRepository.findByGroupIdAndUserId(groupId, userId)).thenReturn(Optional.empty());

        CommunityDtos.CommunityResponse response = newService().leave(userId, groupId);

        assertFalse(response.member());
        verify(memberRepository, never()).deleteByGroupIdAndUserId(groupId, userId);
    }

    @Test
    void membershipIsNotExposedToAnonymousVisitors() {
        Page<CommunityGroup> page = new PageImpl<>(List.of(group()));
        when(groupRepository.findByActiveTrue(any(Pageable.class))).thenReturn(page);

        CommunityDtos.CommunityResponse response =
                newService().search(null, null, 0, 20).content().get(0);

        assertFalse(response.member());
        assertFalse(response.moderator());
    }

    @Test
    void searchWithoutTermReturnsActiveGroups() {
        Page<CommunityGroup> page = new PageImpl<>(List.of(group()));
        when(groupRepository.findByActiveTrue(any(Pageable.class))).thenReturn(page);

        List<CommunityDtos.CommunityResponse> result =
                newService().search("   ", null, 0, 20).content();

        assertEquals(1, result.size());
        assertEquals("Paseos en Retiro", result.get(0).name());
    }

    @Test
    void searchWithTopicFiltersByTopic() {
        Page<CommunityGroup> page = new PageImpl<>(List.of(group()));
        when(groupRepository.findByActiveTrueAndTopic(eq("LOCATION"), any(Pageable.class)))
                .thenReturn(page);

        newService().search(null, "LOCATION", 0, 20);

        verify(groupRepository).findByActiveTrueAndTopic(eq("LOCATION"), any(Pageable.class));
    }

    @Test
    void unknownCommunityFails() {
        when(groupRepository.findById(groupId)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> newService().join(userId, groupId));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    // ========== CRUD ==========

    @Test
    void createPersistsGroupAndMakesCreatorModerator() {
        when(groupRepository.save(any(CommunityGroup.class))).thenAnswer(invocation -> {
            CommunityGroup saved = invocation.getArgument(0);
            // Sin id el servicio no puede resolver la pertenencia en toResponse.
            if (saved.getId() == null) {
                saved.setId(groupId);
            }
            return saved;
        });
        when(memberRepository.save(any(CommunityGroupMember.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(memberRepository.findByGroupIdAndUserId(groupId, userId)).thenReturn(Optional
                .of(new CommunityGroupMember(groupId, userId, CommunityGroupMember.Role.MODERATOR)));

        CommunityDtos.CommunityResponse response =
                newService().create(userId, createRequest("  Paseos en Retiro  ", "location"));

        assertEquals("Paseos en Retiro", response.name());
        assertEquals("LOCATION", response.topic());
        assertEquals(userId.toString(), response.createdBy());
        assertTrue(response.owner());
        // El creador entra como miembro, no queda colgando fuera de su circulo.
        assertTrue(response.member());
        assertTrue(response.moderator());
        assertEquals(1, response.membersCount());
    }

    @Test
    void createRejectsBlankName() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> newService().create(userId, createRequest("   ", null)));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void createRejectsUnknownTopic() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> newService().create(userId, createRequest("Paseos", "POLITICA")));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void updateRejectsNonOwner() {
        // Sin createdBy nadie es propietario, ni siquiera un miembro.
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group()));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> newService().update(userId, groupId, updateRequest("Nuevo", null)));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(groupRepository, never()).save(any(CommunityGroup.class));
    }

    @Test
    void updatePersistsChangesForOwner() {
        CommunityGroup group = ownedGroup();
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(groupRepository.save(group)).thenReturn(group);
        when(memberRepository.findByGroupIdAndUserId(groupId, userId)).thenReturn(Optional.empty());

        CommunityDtos.CommunityResponse response =
                newService().update(userId, groupId, updateRequest("Cambio de nombre", null));

        assertEquals("Cambio de nombre", response.name());
        assertEquals("LOCATION", group.getCategory());
        assertTrue(response.owner());
    }

    @Test
    void deleteIsOnlyAllowedForOwner() {
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group()));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> newService().delete(userId, groupId));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(groupRepository, never()).save(any(CommunityGroup.class));
    }

    @Test
    void deleteDeactivatesInsteadOfRemovingRow() {
        CommunityGroup group = ownedGroup();
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(groupRepository.save(group)).thenReturn(group);

        newService().delete(userId, groupId);

        // Borrado logico: la fila se conserva para no arrastrar las membresias.
        assertFalse(group.isActive());
        verify(groupRepository).save(group);
    }

    @Test
    void inactiveCommunityIsNotFound() {
        CommunityGroup group = ownedGroup();
        group.setActive(false);
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> newService().join(userId, groupId));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void myCommunitiesSkipsDeactivatedCommunities() {
        CommunityGroup deleted = ownedGroup();
        deleted.setActive(false);
        when(memberRepository.findByUserId(eq(userId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(
                        new CommunityGroupMember(groupId, userId, CommunityGroupMember.Role.MEMBER)),
                        PageRequest.of(0, 20), 1));
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(deleted));

        assertTrue(newService().myCommunities(userId, 0, 20).content().isEmpty());
    }
}
