package com.petconnect.users.presentation.rest;

import com.petconnect.shared.infrastructure.security.CustomUserDetails;
import com.petconnect.users.application.usecases.CompleteOnboardingUseCase;
import com.petconnect.users.application.usecases.GetUserProfileUseCase;
import com.petconnect.users.application.usecases.GetUserPreferencesUseCase;
import com.petconnect.users.application.usecases.SearchProvidersUseCase;
import com.petconnect.users.application.usecases.UpdateProfileUseCase;
import com.petconnect.users.application.usecases.UpdateProfilePreferencesUseCase;
import com.petconnect.users.domain.UserProfile;
import com.petconnect.users.domain.exceptions.UserNotFoundException;
import com.petconnect.users.domain.repositories.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.beans.factory.ObjectProvider;
import com.petconnect.messages.infrastructure.websocket.WebSocketPresenceListener;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserProfileControllerTest {

    private UserProfileRepository userProfileRepository;
    private UserProfileController controller;

    @BeforeEach
    void setUp() {
        userProfileRepository = mock(UserProfileRepository.class);
        controller = new UserProfileController(
                mock(UpdateProfileUseCase.class),
                mock(GetUserProfileUseCase.class),
                mock(GetUserPreferencesUseCase.class),
                mock(SearchProvidersUseCase.class),
                mock(CompleteOnboardingUseCase.class),
                mock(UpdateProfilePreferencesUseCase.class),
                userProfileRepository,
                mock(ObjectProvider.class));
    }

    @Test
    void publicProfileDirectoryOmitsPrivateProfilesAndPersonalFields() {
        UUID publicUserId = UUID.randomUUID();
        UserProfile publicProfile = profile(publicUserId, true);
        UserProfile privateProfile = profile(UUID.randomUUID(), false);
        when(userProfileRepository.findAll()).thenReturn(List.of(publicProfile, privateProfile));

        var response = controller.getAllProfiles(null).getBody();

        assertEquals(1, response.size());
        assertNull(response.get(0).phone());
        assertNull(response.get(0).dateOfBirth());
        assertFalse(response.get(0).notificationsEnabled());
    }

    @Test
    void privateProfileIsNotVisibleToOtherUsers() {
        UUID profileId = UUID.randomUUID();
        UserProfile privateProfile = profile(UUID.randomUUID(), false);
        when(userProfileRepository.findById(profileId)).thenReturn(Optional.of(privateProfile));

        assertThrows(
                UserNotFoundException.class,
                () -> controller.getProfileById(user(UUID.randomUUID()), profileId));
    }

    @Test
    void ownerCanReadTheirPrivateProfileFields() {
        UUID authUserId = UUID.randomUUID();
        UserProfile privateProfile = profile(authUserId, false);
        when(userProfileRepository.findById(privateProfile.getId())).thenReturn(Optional.of(privateProfile));

        var response = controller.getProfileById(user(authUserId), privateProfile.getId()).getBody();

        assertEquals("555-0101", response.phone());
        assertEquals(LocalDate.of(1990, 1, 1), response.dateOfBirth());
    }

    private static UserProfile profile(UUID authUserId, boolean isPublic) {
        UserProfile profile = new UserProfile(authUserId, "Ana", "García", "user");
        profile.updateProfile("Ana", "García", "555-0101", "Bio", LocalDate.of(1990, 1, 1));
        profile.setProfilePublic(isPublic);
        return profile;
    }

    private static CustomUserDetails user(UUID userId) {
        return new CustomUserDetails(
                userId,
                "test-user",
                "password",
                true,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }
}
