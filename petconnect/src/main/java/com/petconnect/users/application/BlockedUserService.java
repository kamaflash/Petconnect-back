package com.petconnect.users.application;

import com.petconnect.users.application.dto.BlockedUserResponse;
import com.petconnect.users.domain.BlockedUser;
import com.petconnect.users.domain.exceptions.UserNotFoundException;
import com.petconnect.users.domain.repositories.BlockedUserRepository;
import com.petconnect.users.domain.repositories.UserProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class BlockedUserService {

    private final BlockedUserRepository blockedUserRepository;
    private final UserProfileRepository userProfileRepository;

    public BlockedUserService(
            BlockedUserRepository blockedUserRepository,
            UserProfileRepository userProfileRepository) {
        this.blockedUserRepository = blockedUserRepository;
        this.userProfileRepository = userProfileRepository;
    }

    @Transactional(readOnly = true)
    public List<BlockedUserResponse> list(UUID blockerUserId) {
        return blockedUserRepository.findByBlockerAuthUserIdOrderByCreatedAtDesc(blockerUserId).stream()
                .map(block -> userProfileRepository.findByAuthUserId(block.getBlockedAuthUserId())
                        .map(profile -> new BlockedUserResponse(
                                block.getBlockedAuthUserId(),
                                profile.getFirstName(),
                                profile.getLastName(),
                                profile.getAvatarUrl()))
                        .orElseThrow(() -> new UserNotFoundException(
                                "Profile not found for blocked user: " + block.getBlockedAuthUserId())))
                .toList();
    }

    @Transactional
    public void block(UUID blockerUserId, UUID targetUserId) {
        if (blockerUserId.equals(targetUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot block your own account");
        }
        userProfileRepository.findByAuthUserId(targetUserId)
                .orElseThrow(() -> new UserNotFoundException("User profile not found"));
        if (!blockedUserRepository.existsByBlockerAuthUserIdAndBlockedAuthUserId(blockerUserId, targetUserId)) {
            blockedUserRepository.save(new BlockedUser(blockerUserId, targetUserId));
        }
    }

    @Transactional
    public void unblock(UUID blockerUserId, UUID targetUserId) {
        blockedUserRepository.deleteByBlockerAuthUserIdAndBlockedAuthUserId(blockerUserId, targetUserId);
    }

    @Transactional(readOnly = true)
    public boolean isBlocked(UUID firstUserId, UUID secondUserId) {
        return blockedUserRepository.existsBlockBetween(firstUserId, secondUserId);
    }

    @Transactional(readOnly = true)
    public List<UUID> blockedUserIds(UUID blockerUserId) {
        return blockedUserRepository.findByBlockerAuthUserIdOrderByCreatedAtDesc(blockerUserId).stream()
                .map(block -> block.getBlockedAuthUserId())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UUID> relatedUserIds(UUID userId) {
        var related = new java.util.LinkedHashSet<>(blockedUserIds(userId));
        blockedUserRepository.findByBlockedAuthUserId(userId).stream()
                .map(block -> block.getBlockerAuthUserId())
                .forEach(related::add);
        return List.copyOf(related);
    }

    @Transactional(readOnly = true)
    public List<UUID> relatedProfileIds(UUID userId) {
        return relatedUserIds(userId).stream()
                .map(blockedUserId -> userProfileRepository.findByAuthUserId(blockedUserId)
                        .map(profile -> profile.getId())
                        .orElseThrow(() -> new UserNotFoundException(
                                "Profile not found for blocked user: " + blockedUserId)))
                .toList();
    }
}
