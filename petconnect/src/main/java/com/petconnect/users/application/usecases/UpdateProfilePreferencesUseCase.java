package com.petconnect.users.application.usecases;

import com.petconnect.users.application.dto.UpdateProfilePreferencesRequest;
import com.petconnect.users.application.dto.UserPreferencesResponse;
import com.petconnect.users.domain.UserProfile;
import com.petconnect.users.domain.exceptions.UserNotFoundException;
import com.petconnect.users.domain.repositories.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UpdateProfilePreferencesUseCase {

    private final UserProfileRepository userProfileRepository;

    public UpdateProfilePreferencesUseCase(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    @Transactional
    public UserPreferencesResponse execute(UUID authUserId, UpdateProfilePreferencesRequest request) {
        var profile = userProfileRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new UserNotFoundException("Profile not found for user: " + authUserId));

        profile.setProfilePublic(request.profilePublic());
        profile.setNotificationsEnabled(request.notificationsEnabled());
        profile.setDirectMessagePolicy(request.directMessagePolicy());
        profile.setShowOnlineStatus(request.showOnlineStatus());
        profile.setNotifyMessages(request.notifyMessages());
        profile.setFeedDefault(request.feedDefault());
        profile = userProfileRepository.save(profile);
        return new UserPreferencesResponse(
                profile.isProfilePublic(),
                profile.isNotificationsEnabled(),
                profile.getDirectMessagePolicy(),
                profile.isShowOnlineStatus(),
                profile.isNotifyMessages(),
                profile.getFeedDefault());
    }
}
