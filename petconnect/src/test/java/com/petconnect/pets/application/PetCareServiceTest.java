package com.petconnect.pets.application;

import com.petconnect.pets.application.dto.PetCareDtos;
import com.petconnect.pets.domain.Pet;
import com.petconnect.pets.domain.PetCareRole;
import com.petconnect.pets.domain.PetCareShare;
import com.petconnect.pets.domain.PetCareShareStatus;
import com.petconnect.pets.domain.Species;
import com.petconnect.pets.domain.repositories.PetRepository;
import com.petconnect.pets.infrastructure.persistence.SpringDataPetCareActivityRepository;
import com.petconnect.pets.infrastructure.persistence.SpringDataPetCareShareRepository;
import com.petconnect.users.domain.repositories.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PetCareServiceTest {

    @Mock
    private PetRepository petRepository;
    @Mock
    private UserProfileRepository userProfileRepository;
    @Mock
    private SpringDataPetCareShareRepository shareRepository;
    @Mock
    private SpringDataPetCareActivityRepository activityRepository;

    private PetCareService service;
    private final UUID ownerId = UUID.randomUUID();
    private final UUID petId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new PetCareService(petRepository, userProfileRepository, shareRepository, activityRepository);
    }

    @Test
    void passportIsTokenizedAndPrivateByDefault() {
        Pet pet = pet();
        when(petRepository.findById(petId)).thenReturn(Optional.of(pet));
        when(petRepository.save(pet)).thenReturn(pet);
        when(petRepository.findByPassportToken(any())).thenReturn(Optional.of(pet));

        PetCareDtos.PassportSettings settings = service.configurePassport(
                ownerId, petId, new PetCareDtos.PassportUpdate(true, false, false, false));

        assertNotNull(settings.token());
        assertEquals(43, settings.token().length());
        assertNull(service.getPublicPassport(settings.token()).allergies());
        verify(petRepository).save(pet);
    }

    @Test
    void onlyOwnerCanConfigurePassport() {
        when(petRepository.findById(petId)).thenReturn(Optional.of(pet()));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.configurePassport(UUID.randomUUID(), petId,
                        new PetCareDtos.PassportUpdate(true, false, false, false)));

        assertEquals(403, exception.getStatusCode().value());
    }

    @Test
    void viewerCannotRecordCareActivity() {
        UUID viewerId = UUID.randomUUID();
        Pet pet = pet();
        PetCareShare share = new PetCareShare(petId, ownerId, viewerId, PetCareRole.VIEWER);
        share.changeStatus(PetCareShareStatus.ACTIVE);
        when(petRepository.findById(petId)).thenReturn(Optional.of(pet));
        when(shareRepository.findByPetIdAndInvitedUserId(petId, viewerId)).thenReturn(Optional.of(share));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.addActivity(viewerId, petId,
                        new PetCareDtos.ActivityRequest(
                                com.petconnect.pets.domain.PetCareActivityType.WALK, "Paseo", null)));

        assertEquals(403, exception.getStatusCode().value());
    }

    @Test
    void activeCaregiverCanRecordActivity() {
        UUID caregiverId = UUID.randomUUID();
        Pet pet = pet();
        PetCareShare share = new PetCareShare(petId, ownerId, caregiverId, PetCareRole.CAREGIVER);
        share.changeStatus(PetCareShareStatus.ACTIVE);
        when(petRepository.findById(petId)).thenReturn(Optional.of(pet));
        when(shareRepository.findByPetIdAndInvitedUserId(petId, caregiverId)).thenReturn(Optional.of(share));
        when(activityRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PetCareDtos.ActivityResponse created = service.addActivity(caregiverId, petId,
                new PetCareDtos.ActivityRequest(
                        com.petconnect.pets.domain.PetCareActivityType.WALK, "Paseo", null));

        assertEquals(caregiverId, created.actorId());
        assertEquals("Paseo", created.notes());
    }

    private Pet pet() {
        Pet pet = new Pet(ownerId, "Luna", Species.CAT);
        pet.setId(petId);
        return pet;
    }
}
