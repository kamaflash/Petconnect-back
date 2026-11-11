package com.petconnect.users.application.usecases;

import com.petconnect.users.application.dto.UpdateProfilePreferencesRequest;
import com.petconnect.users.domain.DirectMessagePolicy;
import com.petconnect.users.domain.UserProfile;
import com.petconnect.users.domain.exceptions.UserNotFoundException;
import com.petconnect.users.domain.repositories.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateProfilePreferencesUseCaseTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    private UpdateProfilePreferencesUseCase useCase;

    private final UUID authUserId = UUID.randomUUID();
    private UserProfile profile;

    @BeforeEach
    void setUp() {
        profile = new UserProfile(authUserId, "Ana", "García", "user");
        useCase = new UpdateProfilePreferencesUseCase(userProfileRepository);
    }

    @Test
    void updatesAllSettingsForTheAuthenticatedUserProfile() {
        when(userProfileRepository.findByAuthUserId(authUserId)).thenReturn(Optional.of(profile));
        when(userProfileRepository.save(profile)).thenReturn(profile);

        var result = useCase.execute(authUserId,
                new UpdateProfilePreferencesRequest(
                        false, false, DirectMessagePolicy.FOLLOWING,
                        false, false, "FOLLOWING"));

        assertEquals(DirectMessagePolicy.FOLLOWING, result.directMessagePolicy());
        assertEquals("FOLLOWING", result.feedDefault());
        assertFalse(profile.isProfilePublic());
        assertFalse(profile.isNotificationsEnabled());
        assertFalse(profile.isShowOnlineStatus());
        assertFalse(profile.isNotifyMessages());
        verify(userProfileRepository).save(profile);
    }

    @Test
    void doesNotSaveWhenTheAuthenticatedUserHasNoProfile() {
        when(userProfileRepository.findByAuthUserId(authUserId)).thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> useCase.execute(authUserId,
                        new UpdateProfilePreferencesRequest(
                                false, false, DirectMessagePolicy.NOBODY,
                                false, false, "FOLLOWING")));

        verify(userProfileRepository, never()).save(any(UserProfile.class));
    }
}
