package com.petconnect.social.presentation.rest;

import com.petconnect.social.domain.*;
import com.petconnect.social.infrastructure.persistence.*;
import com.petconnect.users.domain.UserProfile;
import com.petconnect.users.domain.repositories.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import com.petconnect.gamification.application.events.XpEvent;
import com.petconnect.gamification.domain.ActionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/social")
public class SocialController {

    private static final Logger log = LoggerFactory.getLogger(SocialController.class);

    private final SpringDataEventRepository eventRepository;
    private final SpringDataGroupRepository groupRepository;
    private final SpringDataFollowRepository followRepository;
    private final SpringDataNotificationRepository notificationRepository;
    private final SpringDataTrendingTopicRepository trendingTopicRepository;
    private final UserProfileRepository userProfileRepository;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    public SocialController(
            SpringDataEventRepository eventRepository,
            SpringDataGroupRepository groupRepository,
            SpringDataFollowRepository followRepository,
            SpringDataNotificationRepository notificationRepository,
            SpringDataTrendingTopicRepository trendingTopicRepository,
            UserProfileRepository userProfileRepository) {
        this.eventRepository = eventRepository;
        this.groupRepository = groupRepository;
        this.followRepository = followRepository;
        this.notificationRepository = notificationRepository;
        this.trendingTopicRepository = trendingTopicRepository;
        this.userProfileRepository = userProfileRepository;
    }

    // ========== EVENTS ==========

    @GetMapping("/events")
    public ResponseEntity<List<Event>> getAllEvents() {
        log.debug("GET /api/v1/social/events");
        return ResponseEntity.ok(eventRepository.findByActiveTrueOrderByEventDateAsc());
    }

    @PostMapping("/events")
    public ResponseEntity<Event> createEvent(@RequestBody Event event) {
        log.debug("POST /api/v1/social/events");
        return ResponseEntity.ok(eventRepository.save(event));
    }

    @PostMapping("/events/{eventId}/attend")
    public ResponseEntity<Event> attendEvent(@PathVariable UUID eventId) {
        log.debug("POST /api/v1/social/events/{}/attend", eventId);
        return eventRepository.findById(eventId)
                .map(event -> {
                    event.incrementAttendees();
                    return ResponseEntity.ok(eventRepository.save(event));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/events/{eventId}/unattend")
    public ResponseEntity<Event> unattendEvent(@PathVariable UUID eventId) {
        log.debug("POST /api/v1/social/events/{}/unattend", eventId);
        return eventRepository.findById(eventId)
                .map(event -> {
                    event.decrementAttendees();
                    return ResponseEntity.ok(eventRepository.save(event));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ========== GROUPS ==========

    @GetMapping("/groups")
    public ResponseEntity<List<CommunityGroup>> getAllGroups() {
        log.debug("GET /api/v1/social/groups");
        return ResponseEntity.ok(groupRepository.findByActiveTrue());
    }

    @PostMapping("/groups")
    public ResponseEntity<CommunityGroup> createGroup(@RequestBody CommunityGroup group) {
        log.debug("POST /api/v1/social/groups");
        return ResponseEntity.ok(groupRepository.save(group));
    }

    @PostMapping("/groups/{groupId}/join")
    public ResponseEntity<CommunityGroup> joinGroup(@PathVariable UUID groupId) {
        log.debug("POST /api/v1/social/groups/{}/join", groupId);
        return groupRepository.findById(groupId)
                .map(group -> {
                    group.incrementMembers();
                    return ResponseEntity.ok(groupRepository.save(group));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/groups/{groupId}/leave")
    public ResponseEntity<CommunityGroup> leaveGroup(@PathVariable UUID groupId) {
        log.debug("POST /api/v1/social/groups/{}/leave", groupId);
        return groupRepository.findById(groupId)
                .map(group -> {
                    group.decrementMembers();
                    return ResponseEntity.ok(groupRepository.save(group));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ========== FOLLOWS ==========
    // El frontend trabaja con authUserIds (el usuario autenticado y los ids de
    // las rutas /profile/:id), pero la tabla follows referencia a
    // user_profiles(id). Aquí se traduce en ambos sentidos.

    @GetMapping("/follows/followers/{userId}")
    public ResponseEntity<List<Follow>> getFollowers(@PathVariable UUID userId) {
        log.debug("GET /api/v1/social/follows/followers/{}", userId);
        return resolveProfileId(userId)
                .map(followRepository::findByFollowingId)
                .map(this::toAuthIdFollows)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.ok(List.of()));
    }

    @GetMapping("/follows/following/{userId}")
    public ResponseEntity<List<Follow>> getFollowing(@PathVariable UUID userId) {
        log.debug("GET /api/v1/social/follows/following/{}", userId);
        return resolveProfileId(userId)
                .map(followRepository::findByFollowerId)
                .map(this::toAuthIdFollows)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.ok(List.of()));
    }

    @PostMapping("/follows")
    public ResponseEntity<Follow> followUser(@RequestBody Follow follow) {
        log.debug("POST /api/v1/social/follows");
        var followerProfileId = resolveProfileId(follow.getFollowerId());
        var followingProfileId = resolveProfileId(follow.getFollowingId());
        if (followerProfileId.isEmpty() || followingProfileId.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        if (followRepository.existsByFollowerIdAndFollowingId(followerProfileId.get(), followingProfileId.get())) {
            return ResponseEntity.badRequest().build();
        }
        followRepository.save(new Follow(followerProfileId.get(), followingProfileId.get()));
        // El XP usa el authUserId (contrato de XpEvent), no el id de perfil.
        publishGamification(ActionType.FOLLOW, authUserIdByProfileId(followerProfileId.get()));
        // Se responde con los authUserIds que envió el cliente.
        return ResponseEntity.ok(new Follow(follow.getFollowerId(), follow.getFollowingId()));
    }

    @DeleteMapping("/follows/{followerId}/{followingId}")
    public ResponseEntity<Void> unfollowUser(@PathVariable UUID followerId, @PathVariable UUID followingId) {
        log.debug("DELETE /api/v1/social/follows/{}/{}", followerId, followingId);
        var followerProfileId = resolveProfileId(followerId);
        var followingProfileId = resolveProfileId(followingId);
        if (followerProfileId.isPresent() && followingProfileId.isPresent()) {
            followRepository.deleteByFollowerIdAndFollowingId(followerProfileId.get(), followingProfileId.get());
        }
        return ResponseEntity.noContent().build();
    }

    /**
     * Resuelve el id de perfil (FK de follows) a partir de un id recibido del
     * cliente. El frontend mezcla authUserIds e ids de perfil en las rutas
     * /profile/:id según el origen del enlace, así que se aceptan ambos.
     */
    private Optional<UUID> resolveProfileId(UUID id) {
        if (id == null) {
            return Optional.empty();
        }
        return userProfileRepository.findByAuthUserId(id)
                .or(() -> userProfileRepository.findById(id))
                .map(UserProfile::getId);
    }

    /** Devuelve las filas de follows con los ids traducidos a authUserIds. */
    private List<Follow> toAuthIdFollows(List<Follow> follows) {
        return follows.stream()
                .map(f -> new Follow(authUserIdByProfileId(f.getFollowerId()), authUserIdByProfileId(f.getFollowingId())))
                .toList();
    }

    private UUID authUserIdByProfileId(UUID profileId) {
        return userProfileRepository.findById(profileId)
                .map(UserProfile::getAuthUserId)
                .orElse(profileId);
    }

    // ========== NOTIFICATIONS ==========

    @GetMapping("/notifications/{userId}")
    public ResponseEntity<List<Notification>> getNotifications(@PathVariable UUID userId) {
        log.debug("GET /api/v1/social/notifications/{}", userId);
        return ResponseEntity.ok(notificationRepository.findByUserIdOrderByCreatedAtDesc(userId));
    }

    @GetMapping("/notifications/{userId}/unread-count")
    public ResponseEntity<Long> getUnreadNotificationCount(@PathVariable UUID userId) {
        log.debug("GET /api/v1/social/notifications/{}/unread-count", userId);
        return ResponseEntity.ok(notificationRepository.countByUserIdAndReadFalse(userId));
    }

    @PutMapping("/notifications/{notificationId}/read")
    public ResponseEntity<Notification> markNotificationAsRead(@PathVariable UUID notificationId) {
        log.debug("PUT /api/v1/social/notifications/{}/read", notificationId);
        return notificationRepository.findById(notificationId)
                .map(notification -> {
                    notification.markAsRead();
                    return ResponseEntity.ok(notificationRepository.save(notification));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/notifications/read-all/{userId}")
    public ResponseEntity<Void> markAllNotificationsAsRead(@PathVariable UUID userId) {
        log.debug("PUT /api/v1/social/notifications/read-all/{}", userId);
        notificationRepository.findByUserIdAndReadFalse(userId)
                .forEach(notification -> {
                    notification.markAsRead();
                    notificationRepository.save(notification);
                });
        return ResponseEntity.noContent().build();
    }

    // ========== TRENDING TOPICS ==========

    @GetMapping("/trending")
    public ResponseEntity<List<TrendingTopic>> getTrendingTopics() {
        log.debug("GET /api/v1/social/trending");
        return ResponseEntity.ok(trendingTopicRepository.findByActiveTrueOrderByPostsCountDesc());
    }

    // ========== GAMIFICATION ==========

    private void publishGamification(ActionType actionType, UUID userId) {
        if (applicationEventPublisher == null) {
            return;
        }
        try {
            applicationEventPublisher.publishEvent(new XpEvent(userId, actionType));
        } catch (Exception e) {
            log.warn("No se pudo notificar XP de {} para el usuario {}", actionType, userId);
        }
    }
}