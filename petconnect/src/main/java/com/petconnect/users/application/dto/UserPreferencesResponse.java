package com.petconnect.users.application.dto;

import com.petconnect.users.domain.DirectMessagePolicy;

public record UserPreferencesResponse(
        boolean profilePublic,
        boolean notificationsEnabled,
        DirectMessagePolicy directMessagePolicy,
        boolean showOnlineStatus,
        boolean notifyMessages,
        String feedDefault) {
}
