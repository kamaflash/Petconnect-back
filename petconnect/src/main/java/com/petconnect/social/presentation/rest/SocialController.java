package com.petconnect.social.presentation.rest;

import com.petconnect.social.domain.*;
import com.petconnect.social.infrastructure.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    public SocialController(
            SpringDataEventRepository eventRepository,
            SpringDataGroupRepository groupRepository,
            SpringDataFollowRepository followRepository,
            SpringDataNotificationRepository notificationRepository,
            SpringDataTrendingTopicRepository trendingTopicRepository) {
        this.eventRepository = eventRepository;
        this.groupRepository = groupRepository;
        this.followRepository = followRepository;
        this.notificationRepository = notificationRepository;
        this.trendingTopicRepository = trendingTopicRepository;
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

    @GetMapping("/follows/followers/{userId}")
    public ResponseEntity<List<Follow>> getFollowers(@PathVariable UUID userId) {
        log.debug("GET /api/v1/social/follows/followers/{}", userId);
        return ResponseEntity.ok(followRepository.findByFollowingId(userId));
    }

    @GetMapping("/follows/following/{userId}")
    public ResponseEntity<List<Follow>> getFollowing(@PathVariable UUID userId) {
        log.debug("GET /api/v1/social/follows/following/{}", userId);
        return ResponseEntity.ok(followRepository.findByFollowerId(userId));
    }

    @PostMapping("/follows")
    public ResponseEntity<Follow> followUser(@RequestBody Follow follow) {
        log.debug("POST /api/v1/social/follows");
        if (followRepository.existsByFollowerIdAndFollowingId(follow.getFollowerId(), follow.getFollowingId())) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(followRepository.save(follow));
    }

    @DeleteMapping("/follows/{followerId}/{followingId}")
    public ResponseEntity<Void> unfollowUser(@PathVariable UUID followerId, @PathVariable UUID followingId) {
        log.debug("DELETE /api/v1/social/follows/{}/{}", followerId, followingId);
        followRepository.deleteByFollowerIdAndFollowingId(followerId, followingId);
        return ResponseEntity.noContent().build();
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
}