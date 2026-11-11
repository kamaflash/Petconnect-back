package com.petconnect.users.presentation.rest;

import com.petconnect.shared.infrastructure.security.CustomUserDetails;
import com.petconnect.users.application.BlockedUserService;
import com.petconnect.users.application.dto.BlockedUserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/blocked")
public class BlockedUserController {

    private final BlockedUserService blockedUserService;

    public BlockedUserController(BlockedUserService blockedUserService) {
        this.blockedUserService = blockedUserService;
    }

    @GetMapping
    public ResponseEntity<List<BlockedUserResponse>> getBlockedUsers(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(blockedUserService.list(userDetails.getUserId()));
    }

    @PostMapping("/{userId}")
    public ResponseEntity<Void> blockUser(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID userId) {
        blockedUserService.block(userDetails.getUserId(), userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> unblockUser(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID userId) {
        blockedUserService.unblock(userDetails.getUserId(), userId);
        return ResponseEntity.noContent().build();
    }
}
