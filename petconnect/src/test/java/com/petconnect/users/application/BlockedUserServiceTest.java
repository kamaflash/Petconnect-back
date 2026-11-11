package com.petconnect.users.application;

import com.petconnect.users.domain.BlockedUser;
import com.petconnect.users.domain.UserProfile;
import com.petconnect.users.domain.repositories.BlockedUserRepository;
import com.petconnect.users.domain.repositories.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BlockedUserServiceTest {

    private BlockedUserRepository blockedUserRepository;
    private UserProfileRepository userProfileRepository;
    private BlockedUserService service;

    @BeforeEach
    void setUp() {
        blockedUserRepository = mock(BlockedUserRepository.class);
        userProfileRepository = mock(UserProfileRepository.class);
        service = new BlockedUserService(blockedUserRepository, userProfileRepository);
    }

    @Test
    void blocksAnExistingAccountOnce() {
        UUID blockerId = UUID.randomUUID();
        UUID blockedId = UUID.randomUUID();
        when(userProfileRepository.findByAuthUserId(blockedId))
                .thenReturn(Optional.of(new UserProfile(blockedId, "Blocked", "User", "user")));
        when(blockedUserRepository.existsByBlockerAuthUserIdAndBlockedAuthUserId(blockerId, blockedId))
                .thenReturn(false);

        service.block(blockerId, blockedId);

        verify(blockedUserRepository).save(org.mockito.ArgumentMatchers.argThat(
                block -> block.getBlockerAuthUserId().equals(blockerId)
                        && block.getBlockedAuthUserId().equals(blockedId)));
    }

    @Test
    void doesNotAllowBlockingYourOwnAccount() {
        UUID userId = UUID.randomUUID();

        assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> service.block(userId, userId));

        verify(blockedUserRepository, never()).save(org.mockito.ArgumentMatchers.any(BlockedUser.class));
    }

    @Test
    void listsBlockedAccountsWithDisplayNames() {
        UUID blockerId = UUID.randomUUID();
        UUID blockedId = UUID.randomUUID();
        when(blockedUserRepository.findByBlockerAuthUserIdOrderByCreatedAtDesc(blockerId))
                .thenReturn(List.of(new BlockedUser(blockerId, blockedId)));
        when(userProfileRepository.findByAuthUserId(blockedId))
                .thenReturn(Optional.of(new UserProfile(blockedId, "Ana", "Lopez", "user")));

        var result = service.list(blockerId);

        assertEquals(1, result.size());
        assertEquals(blockedId, result.get(0).userId());
        assertEquals("Ana", result.get(0).firstName());
    }
}
