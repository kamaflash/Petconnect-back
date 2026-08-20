package com.petconnect.gamification.application;

import com.petconnect.gamification.application.dto.AchievementResponse;
import com.petconnect.gamification.application.dto.GamificationSummaryResponse;
import com.petconnect.gamification.application.dto.GoalResponse;
import com.petconnect.gamification.domain.AchievementCatalog;
import com.petconnect.gamification.domain.AchievementState;
import com.petconnect.gamification.domain.ActionType;
import com.petconnect.gamification.domain.GoalCatalog;
import com.petconnect.gamification.domain.GoalType;
import com.petconnect.gamification.domain.UserAchievement;
import com.petconnect.gamification.domain.UserGamification;
import com.petconnect.gamification.domain.UserGoalProgress;
import com.petconnect.gamification.domain.XpTransaction;
import com.petconnect.gamification.domain.repositories.AchievementCatalogRepository;
import com.petconnect.gamification.domain.repositories.GoalCatalogRepository;
import com.petconnect.gamification.domain.repositories.UserAchievementRepository;
import com.petconnect.gamification.domain.repositories.UserGamificationRepository;
import com.petconnect.gamification.domain.repositories.UserGoalProgressRepository;
import com.petconnect.gamification.domain.repositories.XpTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Motor de gamificación: otorga XP por acciones, gestiona subida de nivel,
 * desbloqueo de logros fijos y progresión de objetivos variables
 * (diarios/semanales). Incluye anti-grind con límites diarios por acción y
 * tope diario de XP. El usuario comienza en el nivel 0 con 0 XP.
 */
@Service
public class GamificationService {

    private static final Logger log = LoggerFactory.getLogger(GamificationService.class);

    /** Tope máximo de XP que un usuario puede ganar en un día. */
    public static final int MAX_DAILY_XP = 1500;

    /** Límite diario por tipo de acción para evitar granjas de XP. */
    private static final Map<ActionType, Integer> DAILY_ACTION_CAP = new HashMap<>() {
        {
            put(ActionType.LIKE, 20);
            put(ActionType.CREATE_COMMENT, 30);
            put(ActionType.FOLLOW, 15);
            put(ActionType.GENERATE_POST, 10);
        }
    };

// =================================================================
    //  Concesión de XP (principal)
    // =================================================================

    @Transactional
    public AwardResult awardXp(UUID userId, ActionType actionType) {
        LocalDate today = LocalDate.now();
        UserGamification gamification = createIfMissing(userId);
        int oldLevel = gamification.getLevel();

        int xp = computeAllowedXp(userId, actionType, today, gamification);
        if (xp > 0) {
            gamification.addXp(xp, today);
        }

        List<String> completedGoalCodes = advanceGoals(userId, actionType);
        List<String> unlockedAchievements = advanceAchievements(gamification, actionType);

        int newLevel = gamification.getLevel();
        if (xp > 0 || !unlockedAchievements.isEmpty()) {
            userGamificationRepository.save(gamification);
            xpTransactionRepository.save(new XpTransaction(userId, actionType, xp, today));
        }

        return new AwardResult(userId, actionType, xp, oldLevel, newLevel, gamification.getTotalXp(),
                unlockedAchievements, completedGoalCodes);
    }

    // =================================================================
    //  Lecturas para el frontend
    // =================================================================

    @Transactional
    public GamificationSummaryResponse getSummary(UUID userId) {
        UserGamification g = createIfMissing(userId);
        return new GamificationSummaryResponse(
                g.getLevel(),
                g.getCurrentLevelXp(),
                g.getXpToLevel(),
                g.getTotalXp(),
                g.getRankTitle(),
                g.getStreakDays(),
                g.getDailyXpEarned(),
                MAX_DAILY_XP,
                g.getDailyXpDate());
    }

    @Transactional
    public List<AchievementResponse> getAchievements(UUID userId) {
        List<AchievementCatalog> catalog = achievementCatalogRepository.findAll();
        Map<String, UserAchievement> userMap = new HashMap<>();
        for (UserAchievement ua : userAchievementRepository.findAllByUserId(userId)) {
            userMap.put(ua.getAchievementCode(), ua);
        }
        return catalog.stream().map(c -> {
            UserAchievement ua = userMap.get(c.getCode());
            int progress = ua != null ? ua.getProgress() : 0;
            AchievementState state = ua != null ? ua.getState() : AchievementState.LOCKED;
            boolean canClaim = state == AchievementState.UNLOCKED;
            String unlockedAt = ua != null && ua.getUnlockedAt() != null ? ua.getUnlockedAt().toString() : null;
            return new AchievementResponse(c.getId().toString(), c.getCode(), c.getName(), c.getDescription(),
                    c.getIcon(), c.getCategory(), progress, c.getTargetValue(), c.getXpReward(), state, canClaim,
                    unlockedAt);
        }).toList();
    }

    @Transactional
    public List<GoalResponse> getGoals(UUID userId) {
        return goalCatalogRepository.findAllActive().stream()
                .map(goal -> toGoalResponse(userId, goal))
                .sorted(Comparator.comparing((GoalResponse g) -> g.type().name()).thenComparing(GoalResponse::code))
                .toList();
    }

    /**
     * Reclama la recompensa de un objetivo variable completado (diario/semanal).
     * Devuelve la meta actualizada y el nuevo resumen del usuario.
     */
    @Transactional
    public ClaimResult claimGoal(UUID userId, String goalCode) {
        GoalCatalog catalog = goalCatalogRepository.findAllActive().stream()
                .filter(g -> g.getCode().equals(goalCode))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Objetivo no encontrado: " + goalCode));

        UserGoalProgress progress = getOrCreateGoalProgress(userId, catalog);
        if (!progress.canClaim(catalog)) {
            throw new IllegalStateException("El objetivo no está completo o ya fue reclamado");
        }
        progress.claim();
        userGoalProgressRepository.save(progress);

        UserGamification g = createIfMissing(userId);
        g.addXp(catalog.getXpReward(), LocalDate.now());
        userGamificationRepository.save(g);

        return new ClaimResult(toGoalResponse(userId, catalog), getSummary(userId), catalog.getXpReward());
    }
    private final UserGamificationRepository userGamificationRepository;
    private final AchievementCatalogRepository achievementCatalogRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final GoalCatalogRepository goalCatalogRepository;
    private final UserGoalProgressRepository userGoalProgressRepository;
    private final XpTransactionRepository xpTransactionRepository;

    public GamificationService(UserGamificationRepository userGamificationRepository,
            AchievementCatalogRepository achievementCatalogRepository,
            UserAchievementRepository userAchievementRepository,
            GoalCatalogRepository goalCatalogRepository,
            UserGoalProgressRepository userGoalProgressRepository,
            XpTransactionRepository xpTransactionRepository) {
        this.userGamificationRepository = userGamificationRepository;
        this.achievementCatalogRepository = achievementCatalogRepository;
        this.userAchievementRepository = userAchievementRepository;
        this.goalCatalogRepository = goalCatalogRepository;
        this.userGoalProgressRepository = userGoalProgressRepository;
        this.xpTransactionRepository = xpTransactionRepository;
    }
// =================================================================
    //  Helpers
    // =================================================================

    private UserGamification createIfMissing(UUID userId) {
        return userGamificationRepository.findByUserId(userId)
                .orElseGet(() -> userGamificationRepository.save(new UserGamification(userId)));
    }

    /** Determina el XP real a otorgar, aplicando anti-grind y tope diario. */
    private int computeAllowedXp(UUID userId, ActionType actionType, LocalDate today, UserGamification g) {
        Integer cap = DAILY_ACTION_CAP.get(actionType);
        if (cap != null) {
            long used = xpTransactionRepository.findAllByUserIdAndXpOn(userId, today).stream()
                    .filter(tx -> tx.getActionType() == actionType)
                    .count();
            if (used >= cap) {
                return 0;
            }
        }
        int reward = actionType.getXpReward();
        int remainingDaily = MAX_DAILY_XP - g.getDailyXpEarned();
        return Math.max(0, Math.min(reward, remainingDaily));
    }

    // ----- Objetivos variables -----

    private List<String> advanceGoals(UUID userId, ActionType actionType) {
        List<String> completed = new ArrayList<>();
        goalCatalogRepository.findAllActive().stream()
                .filter(goal -> goal.getSourceAction() == actionType)
                .forEach(goal -> {
                    UserGoalProgress p = getOrCreateGoalProgress(userId, goal);
                    boolean wasComplete = p.canClaim(goal);
                    p.increment(goal);
                    userGoalProgressRepository.save(p);
                    if (!wasComplete && p.canClaim(goal)) {
                        completed.add(goal.getCode());
                    }
                });
        return completed;
    }

        private UserGoalProgress getOrCreateGoalProgress(UUID userId, GoalCatalog catalog) {
        GoalType type = catalog.getType();
        LocalDateTime start = periodStart(type);
        LocalDateTime end = periodEnd(type);
        return userGoalProgressRepository.findByUserIdAndGoalCode(userId, catalog.getCode())
                .map(p -> {
                    if (!withinPeriod(p)) {
                        p.reset(type, start, end); // reinicio de día/semana: reutiliza la fila (única por usuario+meta)
                    }
                    return userGoalProgressRepository.save(p);
                })
                .orElseGet(() -> userGoalProgressRepository.save(
                        new UserGoalProgress(userId, catalog.getCode(), type, start, end)));
    }

    private GoalResponse toGoalResponse(UUID userId, GoalCatalog catalog) {
        UserGoalProgress p = getOrCreateGoalProgress(userId, catalog);
        return new GoalResponse(
                catalog.getId().toString(),
                catalog.getCode(),
                catalog.getName(),
                catalog.getDescription(),
                catalog.getIcon(),
                catalog.getType(),
                p.getProgress(),
                catalog.getTargetValue(),
                catalog.getXpReward(),
                p.isClaimed(),
                p.canClaim(catalog),
                p.getPeriodStart(),
                p.getPeriodEnd());
    }

    private boolean withinPeriod(UserGoalProgress p) {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(p.getPeriodStart()) && now.isBefore(p.getPeriodEnd());
    }

    private LocalDateTime periodStart(GoalType type) {
        LocalDate today = LocalDate.now();
        if (type == GoalType.DAILY) {
            return today.atStartOfDay();
        }
        return today.with(DayOfWeek.MONDAY).atStartOfDay();
    }

    private LocalDateTime periodEnd(GoalType type) {
        LocalDate today = LocalDate.now();
        if (type == GoalType.DAILY) {
            return today.atTime(LocalTime.MAX);
        }
        return today.with(DayOfWeek.MONDAY).atTime(LocalTime.MAX).plusDays(6);
    }
// ----- Logros fijos -----

    private static List<String> achievementCodesForGoal(ActionType actionType) {
        return switch (actionType) {
            case GENERATE_POST -> List.of("first_post", "ten_posts");
            case CREATE_COMMENT -> List.of("fifty_comments", "helper_heart");
            case LIKE -> List.of("like_dropper");
            case FOLLOW -> List.of("follow_ten");
            case PET_REGISTERED -> List.of("first_pet", "three_pets");
            case PROFILE_COMPLETED -> List.of("profile_complete");
            case APPOINTMENT_CREATED -> List.of("vet_visits");
            case PRODUCT_PURCHASED -> List.of("first_purchase");
            case ADOPTION_REQUESTED -> List.of("first_adoption");
            case DAILY_LOGIN -> List.of("daily_login_3");
        };
    }

    private List<String> advanceAchievements(UserGamification gamification, ActionType actionType) {
        UUID userId = gamification.getUserId();
        List<String> newlyUnlocked = new ArrayList<>();
        for (String code : achievementCodesForGoal(actionType)) {
            achievementCatalogRepository.findByCode(code).ifPresent(catalog -> {
                UserAchievement ua = userAchievementRepository
                        .findByUserIdAndAchievementCode(userId, code)
                        .orElseGet(() -> userAchievementRepository.save(new UserAchievement(userId, code)));
                boolean wasUnlocked = ua.canClaim() || ua.getState() == AchievementState.CLAIMED;
                ua.incrementProgress(catalog);
                if (!wasUnlocked && ua.canClaim()) {
                    // Otorgar la recompensa XP y marcar como reclamado automáticamente.
                    gamification.addXp(catalog.getXpReward(), LocalDate.now());
                    ua.claim();
                    newlyUnlocked.add(code);
                    log.info("Achievement unlocked: user={}, achievement={}", userId, code);
                }
                userAchievementRepository.save(ua);
            });
        }
        return newlyUnlocked;
    }

    // =================================================================
    //  Resultados
    // =================================================================

    public record ClaimResult(GoalResponse goal, GamificationSummaryResponse summary, int xpGained) {
    }

    public record AwardResult(UUID userId, ActionType actionType, int xpGained, int oldLevel, int newLevel,
            int totalXp, List<String> unlockedAchievements, List<String> completedGoals) {
        public boolean leveledUp() {
            return newLevel > oldLevel;
        }
    }
}