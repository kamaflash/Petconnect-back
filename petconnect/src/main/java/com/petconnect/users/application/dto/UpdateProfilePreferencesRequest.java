package com.petconnect.users.application.dto;

import jakarta.validation.constraints.NotNull;
import com.petconnect.users.domain.DirectMessagePolicy;
import jakarta.validation.constraints.Pattern;

public record UpdateProfilePreferencesRequest(
        @NotNull Boolean profilePublic,
        @NotNull Boolean notificationsEnabled,
        @NotNull DirectMessagePolicy directMessagePolicy,
        @NotNull Boolean showOnlineStatus,
        @NotNull Boolean notifyMessages,
        @NotNull @Pattern(regexp = "ALL|FOLLOWING") String feedDefault) {
}
