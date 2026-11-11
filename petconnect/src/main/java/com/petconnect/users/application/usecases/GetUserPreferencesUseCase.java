package com.petconnect.users.application.usecases;

import com.petconnect.users.application.dto.UserPreferencesResponse;
import com.petconnect.users.domain.exceptions.UserNotFoundException;
import com.petconnect.users.domain.repositories.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class GetUserPreferencesUseCase {

    private final UserProfileRepository userProfileRepository;

    public GetUserPreferencesUseCase(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    @Transactional(readOnly = true)
    public UserPreferencesResponse execute(UUID authUserId) {
        var profile = userProfileRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new UserNotFoundException("Profile not found for user: " + authUserId));
        return new UserPreferencesResponse(
                profile.isProfilePublic(),
                profile.isNotificationsEnabled(),
                profile.getDirectMessagePolicy(),
                profile.isShowOnlineStatus(),
                profile.isNotifyMessages(),
                profile.getFeedDefault());
    }
}
