package com.petconnect.shared.infrastructure.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@ConditionalOnProperty(name = "websocket.enabled", havingValue = "true", matchIfMissing = false)
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Send a notification to a specific user
     */
    public void sendToUser(UUID userId, String type, Object payload) {
        var notification = Map.of(
                "type", type,
                "payload", payload,
                "timestamp", System.currentTimeMillis());
        var destination = "/queue/notifications";
        log.debug("Sending WS notification to user {}: type={}", userId, type);
        messagingTemplate.convertAndSendToUser(userId.toString(), destination, notification);
    }

    /**
     * Send a notification to a topic (broadcast)
     */
    public void sendToTopic(String topic, String type, Object payload) {
        var notification = Map.of(
                "type", type,
                "payload", payload,
                "timestamp", System.currentTimeMillis());
        var destination = "/topic/" + topic;
        log.debug("Sending WS notification to topic {}: type={}", topic, type);
        messagingTemplate.convertAndSend(destination, notification);
    }

    // ========== CONVENIENCE METHODS ==========

    public void notifyNewFollower(UUID userId, String followerName) {
        sendToUser(userId, "NEW_FOLLOWER", Map.of("followerName", followerName));
    }

    public void notifyNewLike(UUID postOwnerId, UUID postId, String likerName) {
        sendToUser(postOwnerId, "NEW_LIKE", Map.of(
                "postId", postId.toString(),
                "likerName", likerName));
    }

    public void notifyNewComment(UUID postOwnerId, UUID postId, String commenterName) {
        sendToUser(postOwnerId, "NEW_COMMENT", Map.of(
                "postId", postId.toString(),
                "commenterName", commenterName));
    }

    public void notifyNewMessage(UUID receiverId, String senderName, String preview) {
        sendToUser(receiverId, "NEW_MESSAGE", Map.of(
                "senderName", senderName,
                "preview", preview));
    }

    public void notifyAchievementUnlocked(UUID userId, String achievementName) {
        sendToUser(userId, "ACHIEVEMENT_UNLOCKED", Map.of(
                "achievementName", achievementName));
    }

    public void notifyLevelUp(UUID userId, int newLevel) {
        sendToUser(userId, "LEVEL_UP", Map.of("newLevel", newLevel));
    }
}