package com.petconnect.pets.application;

import com.petconnect.pets.application.dto.PetCareDtos;
import com.petconnect.pets.domain.Pet;
import com.petconnect.pets.domain.PetCareActivity;
import com.petconnect.pets.domain.PetCareRole;
import com.petconnect.pets.domain.PetCareShare;
import com.petconnect.pets.domain.PetCareShareStatus;
import com.petconnect.pets.domain.repositories.PetRepository;
import com.petconnect.pets.infrastructure.persistence.SpringDataPetCareActivityRepository;
import com.petconnect.pets.infrastructure.persistence.SpringDataPetCareShareRepository;
import com.petconnect.users.domain.UserProfile;
import com.petconnect.users.domain.repositories.UserProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
public class PetCareService {

    private final PetRepository petRepository;
    private final UserProfileRepository userProfileRepository;
    private final SpringDataPetCareShareRepository shareRepository;
    private final SpringDataPetCareActivityRepository activityRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public PetCareService(PetRepository petRepository,
            UserProfileRepository userProfileRepository,
            SpringDataPetCareShareRepository shareRepository,
            SpringDataPetCareActivityRepository activityRepository) {
        this.petRepository = petRepository;
        this.userProfileRepository = userProfileRepository;
        this.shareRepository = shareRepository;
        this.activityRepository = activityRepository;
    }

    @Transactional
    public PetCareDtos.PassportSettings configurePassport(UUID ownerId, UUID petId,
            PetCareDtos.PassportUpdate update) {
        Pet pet = requireOwnedPet(ownerId, petId);
        if (!update.enabled()) {
            pet.revokePassport();
        } else {
            String token = pet.getPassportToken();
            if (token == null) {
                byte[] bytes = new byte[32];
                secureRandom.nextBytes(bytes);
                token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            }
            pet.configurePassport(token, update.shareMedical(),
                    update.shareEmergencyContact(), update.shareVet());
        }
        petRepository.save(pet);
        return passportSettings(pet);
    }

    @Transactional(readOnly = true)
    public PetCareDtos.PassportSettings getPassportSettings(UUID ownerId, UUID petId) {
        return passportSettings(requireOwnedPet(ownerId, petId));
    }

    @Transactional(readOnly = true)
    public PetCareDtos.CareSummary getCareSummary(UUID userId, UUID petId) {
        Pet pet = requirePet(petId);
        if (pet.getOwnerId().equals(userId)) {
            return new PetCareDtos.CareSummary(
                    pet.getId(), pet.getName(), pet.getSpecies().name(), pet.getAvatarUrl(), true, null);
        }
        PetCareShare share = shareRepository.findByPetIdAndInvitedUserId(petId, userId)
                .filter(item -> item.getStatus() == PetCareShareStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "You do not have access to this pet's care"));
        return new PetCareDtos.CareSummary(
                pet.getId(), pet.getName(), pet.getSpecies().name(), pet.getAvatarUrl(), false, share.getRole());
    }

    @Transactional(readOnly = true)
    public PetCareDtos.PublicPassport getPublicPassport(String token) {
        if (token == null || token.length() != 43) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Passport not found");
        }
        Pet pet = petRepository.findByPassportToken(token)
                .filter(Pet::isActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Passport not found"));
        return new PetCareDtos.PublicPassport(
                pet.getName(),
                pet.getSpecies().name(),
                pet.getBreed(),
                pet.getAvatarUrl(),
                pet.isPassportShareMedical() ? pet.getBloodType() : null,
                pet.isPassportShareMedical() ? pet.getAllergies() : null,
                pet.isPassportShareMedical() ? pet.getMedicalConditions() : null,
                pet.isPassportShareEmergencyContact() ? pet.getEmergencyContact() : null,
                pet.isPassportShareEmergencyContact() ? pet.getEmergencyPhone() : null,
                pet.isPassportShareVet() ? pet.getVetName() : null,
                pet.isPassportShareVet() ? pet.getVetPhone() : null,
                pet.isPassportShareVet() ? pet.getVetAddress() : null);
    }

    @Transactional
    public PetCareDtos.ShareResponse invite(UUID ownerId, UUID petId, PetCareDtos.ShareRequest request) {
        if (request.userId() == null || request.role() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User and role are required");
        }
        Pet pet = requireOwnedPet(ownerId, petId);
        if (ownerId.equals(request.userId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The owner cannot invite themselves");
        }
        userProfileRepository.findByAuthUserId(request.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        PetCareShare share = shareRepository.findByPetIdAndInvitedUserId(petId, request.userId())
                .map(existing -> {
                    if (existing.getStatus() == PetCareShareStatus.ACTIVE
                            || existing.getStatus() == PetCareShareStatus.PENDING) {
                        throw new ResponseStatusException(HttpStatus.CONFLICT,
                                "An active or pending invitation already exists");
                    }
                    existing.changeRole(request.role());
                    existing.changeStatus(PetCareShareStatus.PENDING);
                    return existing;
                })
                .orElseGet(() -> new PetCareShare(petId, ownerId, request.userId(), request.role()));
        return toShareResponse(shareRepository.save(share), pet.getName());
    }

    @Transactional(readOnly = true)
    public List<PetCareDtos.ShareResponse> getShares(UUID ownerId, UUID petId) {
        Pet pet = requireOwnedPet(ownerId, petId);
        return shareRepository.findByPetIdOrderByCreatedAtAsc(petId).stream()
                .map(share -> toShareResponse(share, pet.getName()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PetCareDtos.ShareResponse> getInvitations(UUID userId) {
        return shareRepository.findByInvitedUserIdOrderByCreatedAtDesc(userId).stream()
                .map(share -> {
                    Pet pet = petRepository.findById(share.getPetId()).orElse(null);
                    return toShareResponse(share, pet == null ? "Mascota" : pet.getName());
                })
                .toList();
    }

    @Transactional
    public PetCareDtos.ShareResponse respondToInvitation(UUID userId, UUID shareId,
            PetCareDtos.ShareStatusUpdate update) {
        if (update.status() != PetCareShareStatus.ACTIVE && update.status() != PetCareShareStatus.DECLINED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid invitation response");
        }
        PetCareShare share = shareRepository.findById(shareId)
                .filter(item -> item.getInvitedUserId().equals(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invitation not found"));
        if (share.getStatus() != PetCareShareStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Invitation is no longer pending");
        }
        share.changeStatus(update.status());
        Pet pet = petRepository.findById(share.getPetId()).orElse(null);
        return toShareResponse(shareRepository.save(share), pet == null ? "Mascota" : pet.getName());
    }

    @Transactional
    public PetCareDtos.ShareResponse changeRole(UUID ownerId, UUID petId, UUID shareId,
            PetCareDtos.ShareRoleUpdate update) {
        if (update.role() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role is required");
        }
        Pet pet = requireOwnedPet(ownerId, petId);
        PetCareShare share = shareRepository.findByIdAndPetId(shareId, petId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Care share not found"));
        if (share.getStatus() != PetCareShareStatus.PENDING && share.getStatus() != PetCareShareStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only pending or active access can be changed");
        }
        share.changeRole(update.role());
        return toShareResponse(shareRepository.save(share), pet.getName());
    }

    @Transactional
    public void revokeShare(UUID ownerId, UUID petId, UUID shareId) {
        requireOwnedPet(ownerId, petId);
        PetCareShare share = shareRepository.findByIdAndPetId(shareId, petId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Care share not found"));
        share.changeStatus(PetCareShareStatus.REVOKED);
        shareRepository.save(share);
    }

    @Transactional
    public void leaveShare(UUID userId, UUID shareId) {
        PetCareShare share = shareRepository.findById(shareId)
                .filter(item -> item.getInvitedUserId().equals(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Care share not found"));
        if (share.getStatus() == PetCareShareStatus.REVOKED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Access has already been revoked");
        }
        share.changeStatus(PetCareShareStatus.REVOKED);
        shareRepository.save(share);
    }

    @Transactional(readOnly = true)
    public List<PetCareDtos.ActivityResponse> getActivities(UUID userId, UUID petId) {
        Pet pet = requirePet(petId);
        requireCanView(pet, userId);
        return activityRepository.findTop100ByPetIdOrderByOccurredAtDesc(petId).stream()
                .map(this::toActivityResponse)
                .toList();
    }

    @Transactional
    public PetCareDtos.ActivityResponse addActivity(UUID userId, UUID petId,
            PetCareDtos.ActivityRequest request) {
        if (request.type() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Activity type is required");
        }
        Pet pet = requirePet(petId);
        requireCanRecord(pet, userId);
        String notes = request.notes() == null ? null : request.notes().trim();
        if (notes != null && notes.length() > 500) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Notes exceed 500 characters");
        }
        PetCareActivity activity = new PetCareActivity(
                petId, userId, request.type(), notes,
                request.occurredAt() == null ? LocalDateTime.now() : request.occurredAt());
        return toActivityResponse(activityRepository.save(activity));
    }

    private Pet requireOwnedPet(UUID ownerId, UUID petId) {
        Pet pet = requirePet(petId);
        if (!pet.getOwnerId().equals(ownerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the owner can manage this pet");
        }
        return pet;
    }

    private Pet requirePet(UUID petId) {
        return petRepository.findById(petId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet not found"));
    }

    private void requireCanView(Pet pet, UUID userId) {
        if (!pet.getOwnerId().equals(userId)
                && !shareRepository.existsByPetIdAndInvitedUserIdAndStatus(
                        pet.getId(), userId, PetCareShareStatus.ACTIVE)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot view this care history");
        }
    }

    private void requireCanRecord(Pet pet, UUID userId) {
        if (pet.getOwnerId().equals(userId)) {
            return;
        }
        PetCareShare share = shareRepository.findByPetIdAndInvitedUserId(pet.getId(), userId)
                .filter(item -> item.getStatus() == PetCareShareStatus.ACTIVE
                        && item.getRole() == PetCareRole.CAREGIVER)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Only the owner or an active caregiver can record activities"));
    }

    private PetCareDtos.PassportSettings passportSettings(Pet pet) {
        return new PetCareDtos.PassportSettings(
                pet.getPassportToken() != null,
                pet.getPassportToken(),
                pet.isPassportShareMedical(),
                pet.isPassportShareEmergencyContact(),
                pet.isPassportShareVet());
    }

    private PetCareDtos.ShareResponse toShareResponse(PetCareShare share, String petName) {
        UserProfile owner = userProfileRepository.findByAuthUserId(share.getOwnerId()).orElse(null);
        UserProfile user = userProfileRepository.findByAuthUserId(share.getInvitedUserId()).orElse(null);
        return new PetCareDtos.ShareResponse(
                share.getId(), share.getPetId(), petName, share.getOwnerId(),
                owner == null ? "Usuario" : owner.getFirstName() + " " + owner.getLastName(),
                share.getInvitedUserId(),
                user == null ? "Usuario" : user.getFirstName() + " " + user.getLastName(),
                share.getRole(), share.getStatus(), share.getCreatedAt());
    }

    private PetCareDtos.ActivityResponse toActivityResponse(PetCareActivity activity) {
        UserProfile actor = userProfileRepository.findByAuthUserId(activity.getActorId()).orElse(null);
        return new PetCareDtos.ActivityResponse(
                activity.getId(), activity.getPetId(), activity.getActorId(),
                actor == null ? "Usuario" : actor.getFirstName() + " " + actor.getLastName(),
                activity.getActivityType(), activity.getNotes(), activity.getOccurredAt());
    }
}
