package com.petconnect.social.application;

import com.petconnect.social.application.dto.CommunityDtos;
import com.petconnect.social.domain.CommunityGroup;
import com.petconnect.social.domain.CommunityGroupMember;
import com.petconnect.social.infrastructure.persistence.SpringDataCommunityGroupMemberRepository;
import com.petconnect.social.infrastructure.persistence.SpringDataGroupRepository;
import com.petconnect.shared.presentation.dto.PageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Circulos de comunidad.
 *
 * <p>En esta primera version todos los circulos son publicos: unirse es inmediato
 * y no existe solicitud de acceso. El proyecto no cuenta con un sistema de
 * moderacion, asi que se evita construir uno a medias; si mas adelante se anaden
 * circulos privados, el modelo de membresia ya distingue MEMBER de MODERATOR.
 * </p>
 *
 * <p>La propiedad es la unica autorizacion que se aplica hoy: solo el creador
 * puede editar o eliminar su circulo. No hay roles de moderador asignables ni
 * listado de reportes, y la eliminacion es borrado logico (active = false) para
 * no perder las membresias ni el historial.</p>
 */
@Service
public class CommunityService {

    /** Tematicas admitidas. Debe mantenerse en sincronia con COMMUNITY_TOPICS del frontend. */
    private static final Set<String> VALID_TOPICS =
            Set.of("LOCATION", "SPECIES", "ACTIVITY", "SUPPORT");

    private static final int MAX_NAME_LENGTH = 200;
    private static final int MAX_DESCRIPTION_LENGTH = 1000;
    private static final int MAX_AVATAR_LENGTH = 500;
    private static final int MAX_CATEGORY_LENGTH = 100;

    private static final Logger log = LoggerFactory.getLogger(CommunityService.class);

    private final SpringDataGroupRepository groupRepository;
    private final SpringDataCommunityGroupMemberRepository memberRepository;

    public CommunityService(SpringDataGroupRepository groupRepository,
            SpringDataCommunityGroupMemberRepository memberRepository) {
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<CommunityDtos.CommunityResponse> search(String term, String topic,
            int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), clampSize(size),
                Sort.by(Sort.Direction.DESC, "membersCount"));

        Page<CommunityGroup> result;
        if (term != null && !term.isBlank()) {
            result = (topic != null && !topic.isBlank())
                    ? groupRepository.searchActiveByTopic(topic.trim(), term.trim(), pageable)
                    : groupRepository.searchActive(term.trim(), pageable);
        } else if (topic != null && !topic.isBlank()) {
            result = groupRepository.findByActiveTrueAndTopic(topic.trim(), pageable);
        } else {
            result = groupRepository.findByActiveTrue(pageable);
        }

        return PageResponse.from(result, group -> toResponse(group, null));
    }

    @Transactional(readOnly = true)
    public CommunityDtos.CommunityResponse getById(UUID viewerId, UUID groupId) {
        return toResponse(requireGroup(groupId), viewerId);
    }

    /**
     * Círculos del usuario.
     *
     * <p>Las membresías no se borran al desactivar un círculo, así que aquí hay
     * que descartar los grupos inactivos: un círculo eliminado no debe seguir
     * apareciendo en el listado de quien se unió. Se resuelve cada grupo por id
     * (una consulta por membresía, el mismo coste que ya tenía {@code toResponse})
     * porque entre ambas entidades no hay asociación JPA que permita un join.</p>
     */
    @Transactional(readOnly = true)
    public PageResponse<CommunityDtos.CommunityResponse> myCommunities(UUID userId,
            int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), clampSize(size));
        Page<CommunityGroupMember> memberships = memberRepository.findByUserId(userId, pageable);

        List<CommunityGroup> active = memberships.getContent().stream()
                .map(m -> groupRepository.findById(m.getGroupId()).orElse(null))
                .filter(Objects::nonNull)
                .filter(CommunityGroup::isActive)
                .toList();

        return PageResponse.from(new PageImpl<>(active, pageable, memberships.getTotalElements()),
                g -> toResponse(g, userId));
    }

    /**
     * Une al usuario a un circulo. La base de datos impone el par
     * (circulo, usuario) unico, lo que hace la operacion segura ante
     * peticiones simultaneas: el segundo intento falla por constraint
     * en vez de duplicar la membresia.
     */
    @Transactional
    public CommunityDtos.CommunityResponse join(UUID userId, UUID groupId) {
        CommunityGroup group = requireGroup(groupId);

        if (memberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            // Ya es miembro: devolvemos el estado actual sin duplicar la fila.
            return toResponse(group, userId);
        }

        memberRepository.save(new CommunityGroupMember(groupId, userId,
                CommunityGroupMember.Role.MEMBER));
        group.incrementMembers();
        log.info("Usuario {} se unio al circulo {}", userId, groupId);
        return toResponse(groupRepository.save(group), userId);
    }

    /** Abandona el circulo. No falla si el usuario no era miembro. */
    @Transactional
    public CommunityDtos.CommunityResponse leave(UUID userId, UUID groupId) {
        CommunityGroup group = requireGroup(groupId);

        if (!memberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            return toResponse(group, userId);
        }

        memberRepository.deleteByGroupIdAndUserId(groupId, userId);
        group.decrementMembers();
        log.info("Usuario {} abandono el circulo {}", userId, groupId);
        return toResponse(groupRepository.save(group), userId);
    }

    // ========== PROPIEDAD ==========

    /**
     * Crea un círculo y lo deja en manos de su creador.
     *
     * <p>El creador se da de alta como miembro con rol MODERATOR en la misma
     * transacción. Sin esto no podría ver su propio círculo en "mis círculos" ni
     * recibir notificaciones de actividad, y además es lo único que da sentido
     * al indicador {@code moderator} que la respuesta ya exponía.</p>
     */
    @Transactional
    public CommunityDtos.CommunityResponse create(UUID userId,
            CommunityDtos.CreateCommunityRequest request) {
        CommunityGroup group = new CommunityGroup(
                requireName(request.name()),
                clamp(request.description(), MAX_DESCRIPTION_LENGTH),
                clamp(request.avatarUrl(), MAX_AVATAR_LENGTH),
                clamp(request.category(), MAX_CATEGORY_LENGTH));
        group.setTopic(validateTopic(request.topic()));
        group.setCreatedBy(userId);

        group = groupRepository.save(group);
        memberRepository.save(new CommunityGroupMember(group.getId(), userId,
                CommunityGroupMember.Role.MODERATOR));
        group.incrementMembers();

        log.info("Usuario {} creo el circulo {}", userId, group.getId());
        return toResponse(groupRepository.save(group), userId);
    }

    /**
     * Actualiza los datos del círculo. Solo el propietario: el resto recibe 403
     * sin que el motivo sea distinguible de un círculo inexistente.
     */
    @Transactional
    public CommunityDtos.CommunityResponse update(UUID userId, UUID groupId,
            CommunityDtos.UpdateCommunityRequest request) {
        CommunityGroup group = requireGroup(groupId);
        requireOwnership(userId, group);

        group.setName(requireName(request.name()));
        group.setDescription(clamp(request.description(), MAX_DESCRIPTION_LENGTH));
        group.setAvatarUrl(clamp(request.avatarUrl(), MAX_AVATAR_LENGTH));
        group.setCategory(clamp(request.category(), MAX_CATEGORY_LENGTH));
        group.setTopic(validateTopic(request.topic()));

        log.info("Usuario {} actualizo el circulo {}", userId, groupId);
        return toResponse(groupRepository.save(group), userId);
    }

    /**
     * Baja lógica. Se conserva la fila porque borrarla arrastraría las
     * membresías en cascada y con ellas el historial de pertenencias; con
     * {@code active = false} el círculo desaparece de todos los listados y de
     * {@code getById}, que pasan a responder 404.
     */
    @Transactional
    public void delete(UUID userId, UUID groupId) {
        CommunityGroup group = requireGroup(groupId);
        requireOwnership(userId, group);

        group.setActive(false);
        groupRepository.save(group);
        log.info("Usuario {} desactivo el circulo {}", userId, groupId);
    }

    // ========== HELPERS ==========

    /**
     * Círculo visible. Los desactivados responden 404 y no 403: dar a entender
     * que existen revelaría información sobre círculos eliminados.
     */
    private CommunityGroup requireGroup(UUID groupId) {
        return groupRepository.findById(groupId)
                .filter(CommunityGroup::isActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Circulo no encontrado"));
    }

    private void requireOwnership(UUID userId, CommunityGroup group) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado");
        }
        // Un círculo sin createdBy es anterior a la migración de propiedad y no
        // tiene dueño asignable: nadie puede editarlo, ni siquiera sus miembros.
        if (!userId.equals(group.getCreatedBy())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo el propietario del circulo puede modificarlo");
        }
    }

    private String requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre es obligatorio");
        }
        String trimmed = name.trim();
        if (trimmed.length() > MAX_NAME_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El nombre no puede superar " + MAX_NAME_LENGTH + " caracteres");
        }
        return trimmed;
    }

    /** La tematica es opcional pero, si viene, debe ser una de las conocidas. */
    private String validateTopic(String topic) {
        if (topic == null || topic.isBlank()) {
            return null;
        }
        String trimmed = topic.trim().toUpperCase();
        if (!VALID_TOPICS.contains(trimmed)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tematica no valida");
        }
        return trimmed;
    }

    private String clamp(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.length() > maxLength ? trimmed.substring(0, maxLength) : trimmed;
    }

    private int clampSize(int size) {
        return size <= 0 ? 20 : Math.min(size, 100);
    }

    /**
     * Construye la respuesta. viewerId puede ser nulo (visitante sin sesion),
     * en cuyo caso no se expone pertenencia.
     */
    private CommunityDtos.CommunityResponse toResponse(CommunityGroup group, UUID viewerId) {
        boolean member = false;
        boolean moderator = false;
        if (viewerId != null) {
            Optional<CommunityGroupMember> membership =
                    memberRepository.findByGroupIdAndUserId(group.getId(), viewerId);
            member = membership.isPresent();
            moderator = membership.map(CommunityGroupMember::isModerator).orElse(false);
        }

        // owner es la señal que la UI usa para mostrar editar/eliminar. Se deriva
        // del creador y no del rol, de modo que un circulo sin propietario no
        // habilita esas acciones para nadie.
        boolean owner = viewerId != null && viewerId.equals(group.getCreatedBy());

        return new CommunityDtos.CommunityResponse(
                group.getId().toString(),
                group.getName(),
                group.getDescription(),
                group.getAvatarUrl(),
                group.getCategory(),
                group.getTopic(),
                group.getMembersCount(),
                group.getCreatedBy() == null ? null : group.getCreatedBy().toString(),
                member,
                moderator,
                owner);
    }
}
