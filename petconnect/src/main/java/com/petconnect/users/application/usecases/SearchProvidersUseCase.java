package com.petconnect.users.application.usecases;

import com.petconnect.users.application.dto.ProviderSearchResponse;
import com.petconnect.users.application.dto.UserProfileResponse;
import com.petconnect.users.domain.UserProfile;
import com.petconnect.users.domain.repositories.UserProfileRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Busca proveedores (perfiles profesionales) por tipo y, para vets, por los
 * códigos de servicios del catálogo que ofrecen. Solo devuelve perfiles
 * públicos sin cambio de tipo pendiente.
 */
@Service
public class SearchProvidersUseCase {

    /** Tipos de perfil que la página "Servicios cercanos" puede listar. */
    private static final List<String> PROVIDER_TYPES = List.of("vet", "shelter", "seller", "trainer");

    private final UserProfileRepository repository;

    public SearchProvidersUseCase(UserProfileRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public ProviderSearchResponse execute(String profileType, List<String> services, int page, int size) {
        String normalizedType = normalizeType(profileType);
        List<String> normalizedServices = normalizeServices(services);
        int safePage = Math.max(0, page);
        int safeSize = Math.min(50, Math.max(1, size));

        Specification<UserProfile> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isTrue(root.get("profilePublic")));
            predicates.add(cb.isNull(root.get("pendingProfileType")));
            if (normalizedType != null) {
                predicates.add(cb.equal(cb.lower(root.get("profileType")), normalizedType));
            } else {
                predicates.add(cb.lower(root.get("profileType")).in(PROVIDER_TYPES));
            }
            if (!normalizedServices.isEmpty()) {
                // `services` se guarda comma-separated: LIKE sobre el valor con
                // separadores para exigir coincidencia de código completo.
                var padded = cb.concat(",", cb.concat(cb.lower(root.get("services")), ","));
                for (String code : normalizedServices) {
                    predicates.add(cb.like(padded, "%," + code + ",%"));
                }
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };

        var result = repository.findAll(
                spec, PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt")));

        var content = result.getContent().stream().map(this::toResponse).toList();
        return new ProviderSearchResponse(
                content, result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages());
    }

    private UserProfileResponse toResponse(UserProfile profile) {
        return new UserProfileResponse(
                profile.getId(),
                profile.getAuthUserId(),
                profile.getFirstName(),
                profile.getLastName(),
                null,
                profile.getBio(),
                profile.getAvatarUrl(),
                profile.getCoverImageUrl(),
                null,
                profile.getLocation(),
                profile.getLatitude(),
                profile.getLongitude(),
                profile.isProfilePublic(),
                false,
                profile.getProfileType(),
                profile.getSpecialty(),
                profile.getLicenseNumber(),
                profile.getCharityNumber(),
                profile.getStoreName(),
                profile.getWebsite(),
                profile.getPendingProfileType(),
                profile.getServices(),
                profile.isProfileComplete());
    }

    /**
     * Normaliza el tipo pedido: solo se aceptan tipos de proveedor; cualquier
     * otro valor (o vacío) significa "todos los proveedores".
     */
    private static String normalizeType(String profileType) {
        if (profileType == null || profileType.isBlank()) {
            return null;
        }
        String normalized = profileType.trim().toLowerCase(Locale.ROOT);
        return PROVIDER_TYPES.contains(normalized) ? normalized : null;
    }

    private static List<String> normalizeServices(List<String> services) {
        if (services == null) {
            return List.of();
        }
        return services.stream()
                .filter(s -> s != null && !s.isBlank())
                .map(s -> s.trim().toLowerCase(Locale.ROOT))
                .distinct()
                .toList();
    }
}
