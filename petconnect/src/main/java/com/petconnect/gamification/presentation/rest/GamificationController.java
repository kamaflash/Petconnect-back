package com.petconnect.gamification.presentation.rest;

import com.petconnect.gamification.application.GamificationService;
import com.petconnect.gamification.application.dto.AchievementResponse;
import com.petconnect.gamification.application.dto.GamificationSummaryResponse;
import com.petconnect.gamification.application.dto.GoalResponse;
import com.petconnect.shared.infrastructure.security.CurrentUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Endpoints de gamificación para el usuario autenticado.
 */
@RestController
@RequestMapping("/api/v1/gamification")
public class GamificationController {

    private static final Logger log = LoggerFactory.getLogger(GamificationController.class);

    private final GamificationService gamificationService;
    private final CurrentUserService currentUserService;

    public GamificationController(GamificationService gamificationService, CurrentUserService currentUserService) {
        this.gamificationService = gamificationService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/summary")
    public ResponseEntity<GamificationSummaryResponse> getSummary() {
        UUID userId = currentUserService.requireUserId();
        log.debug("GET /api/v1/gamification/summary - user: {}", userId);
        return ResponseEntity.ok(gamificationService.getSummary(userId));
    }

    @GetMapping("/achievements")
    public ResponseEntity<List<AchievementResponse>> getAchievements() {
        UUID userId = currentUserService.requireUserId();
        log.debug("GET /api/v1/gamification/achievements - user: {}", userId);
        return ResponseEntity.ok(gamificationService.getAchievements(userId));
    }

    @GetMapping("/goals")
    public ResponseEntity<List<GoalResponse>> getGoals() {
        UUID userId = currentUserService.requireUserId();
        log.debug("GET /api/v1/gamification/goals - user: {}", userId);
        return ResponseEntity.ok(gamificationService.getGoals(userId));
    }

    @PostMapping("/goals/{goalCode}/claim")
    public ResponseEntity<GamificationService.ClaimResult> claimGoal(@PathVariable String goalCode) {
        UUID userId = currentUserService.requireUserId();
        log.debug("POST /api/v1/gamification/goals/{}/claim - user: {}", goalCode, userId);
        return ResponseEntity.ok(gamificationService.claimGoal(userId, goalCode));
    }
}