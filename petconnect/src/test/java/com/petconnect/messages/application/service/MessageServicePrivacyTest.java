package com.petconnect.messages.application.service;

import com.petconnect.messages.domain.repository.ConversationRepository;
import com.petconnect.messages.domain.repository.MessageRepository;
import com.petconnect.messages.domain.repository.MessageReactionRepository;
import com.petconnect.pets.domain.repositories.PetRepository;
import com.petconnect.pets.domain.Pet;
import com.petconnect.pets.domain.Species;
import com.petconnect.messages.domain.model.Conversation;
import com.petconnect.messages.domain.model.Message;
import com.petconnect.social.domain.repositories.FollowRepository;
import com.petconnect.users.domain.DirectMessagePolicy;
import com.petconnect.users.domain.UserProfile;
import com.petconnect.users.application.BlockedUserService;
import com.petconnect.users.domain.repositories.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MessageServicePrivacyTest {

    private MessageRepository messageRepository;
    private ConversationRepository conversationRepository;
    private MessageReactionRepository messageReactionRepository;
    private UserProfileRepository userProfileRepository;
    private FollowRepository followRepository;
    private PetRepository petRepository;
    private MessageService messageService;

    @BeforeEach
    void setUp() {
        messageRepository = mock(MessageRepository.class);
        conversationRepository = mock(ConversationRepository.class);
        messageReactionRepository = mock(MessageReactionRepository.class);
        userProfileRepository = mock(UserProfileRepository.class);
        followRepository = mock(FollowRepository.class);
        petRepository = mock(PetRepository.class);
        messageService = new MessageService(
                messageRepository,
                conversationRepository,
                userProfileRepository,
                followRepository,
                petRepository,
                messageReactionRepository);
    }

    @Test
    void nobodyPolicyRejectsDirectConversationCreation() {
        UUID senderId = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();
        UserProfile recipient = profile(recipientId);
        recipient.setDirectMessagePolicy(DirectMessagePolicy.NOBODY);
        when(userProfileRepository.findByAuthUserId(recipientId)).thenReturn(Optional.of(recipient));

        assertThrows(
                AccessDeniedException.class,
                () -> messageService.getOrCreateDirectConversation(senderId, recipientId));

        verify(conversationRepository, never()).findDirectBetween(senderId, recipientId);
    }

    @Test
    void followingPolicyAllowsOnlyPeopleTheRecipientFollows() {
        UUID senderId = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();
        UserProfile recipient = profile(recipientId);
        UserProfile sender = profile(senderId);
        recipient.setDirectMessagePolicy(DirectMessagePolicy.FOLLOWING);
        when(userProfileRepository.findByAuthUserId(recipientId)).thenReturn(Optional.of(recipient));
        when(userProfileRepository.findByAuthUserId(senderId)).thenReturn(Optional.of(sender));
        when(followRepository.existsByFollowerIdAndFollowingId(recipient.getId(), sender.getId()))
                .thenReturn(false);

        assertThrows(
                AccessDeniedException.class,
                () -> messageService.getOrCreateDirectConversation(senderId, recipientId));

        verify(followRepository).existsByFollowerIdAndFollowingId(recipient.getId(), sender.getId());
        verify(conversationRepository, never()).findDirectBetween(senderId, recipientId);
    }

    @Test
    void blockBetweenUsersRejectsDirectConversationInEitherDirection() {
        UUID senderId = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();
        BlockedUserService blockedUserService = mock(BlockedUserService.class);
        ReflectionTestUtils.setField(messageService, "blockedUserService", blockedUserService);
        when(blockedUserService.isBlocked(senderId, recipientId)).thenReturn(true);

        assertThrows(
                AccessDeniedException.class,
                () -> messageService.getOrCreateDirectConversation(senderId, recipientId));

        verify(userProfileRepository, never()).findByAuthUserId(recipientId);
        verify(conversationRepository, never()).findDirectBetween(senderId, recipientId);
    }

    @Test
    void usersCannotSharePrivatePetsTheyDoNotOwn() {
        UUID senderId = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        UUID petId = UUID.randomUUID();
        UserProfile sender = profile(senderId);
        Pet privatePet = new Pet(UUID.randomUUID(), "Milo", Species.DOG);
        Conversation conversation = Conversation.builder()
                .id(conversationId)
                .participantIds(java.util.List.of(senderId, recipientId))
                .isGroup(false)
                .build();
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(userProfileRepository.findByAuthUserId(senderId)).thenReturn(Optional.of(sender));
        when(userProfileRepository.findByAuthUserId(recipientId)).thenReturn(Optional.of(profile(recipientId)));
        when(petRepository.findById(petId)).thenReturn(Optional.of(privatePet));

        assertThrows(
                AccessDeniedException.class,
                () -> messageService.sendMessage(senderId, recipientId, conversationId,
                        "Milo", "PET_CARD", null, petId.toString()));

        verify(messageRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void imageMessageRequiresAnUploadedCloudinaryUrl() {
        UUID senderId = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        Conversation conversation = Conversation.builder()
                .id(conversationId)
                .participantIds(java.util.List.of(senderId, recipientId))
                .isGroup(false)
                .build();
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(userProfileRepository.findByAuthUserId(recipientId)).thenReturn(Optional.of(profile(recipientId)));

        assertThrows(
                IllegalArgumentException.class,
                () -> messageService.sendMessage(senderId, recipientId, conversationId,
                        "photo.jpg", "IMAGE", "https://example.com/photo.jpg", null));
    }

    @Test
    void usersCannotReplyToMessagesFromAnotherConversation() {
        UUID senderId = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        UUID otherConversationId = UUID.randomUUID();
        UUID parentId = UUID.randomUUID();
        Conversation conversation = Conversation.builder()
                .id(conversationId)
                .participantIds(java.util.List.of(senderId, recipientId))
                .isGroup(false)
                .build();
        Message parent = Message.builder()
                .id(parentId)
                .conversationId(otherConversationId)
                .senderId(recipientId)
                .build();
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(userProfileRepository.findByAuthUserId(recipientId)).thenReturn(Optional.of(profile(recipientId)));
        when(messageRepository.findById(parentId)).thenReturn(Optional.of(parent));

        assertThrows(
                IllegalArgumentException.class,
                () -> messageService.sendMessage(senderId, recipientId, conversationId,
                        "Reply", "TEXT", null, null, parentId));

        verify(messageRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void replyStoresTheParentReferenceAndQuotePreview() {
        UUID senderId = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        UUID parentId = UUID.randomUUID();
        Conversation conversation = Conversation.builder()
                .id(conversationId)
                .participantIds(java.util.List.of(senderId, recipientId))
                .isGroup(false)
                .build();
        Message parent = Message.builder()
                .id(parentId)
                .conversationId(conversationId)
                .senderId(recipientId)
                .content("Original message")
                .build();
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(userProfileRepository.findByAuthUserId(recipientId)).thenReturn(Optional.of(profile(recipientId)));
        when(messageRepository.findById(parentId)).thenReturn(Optional.of(parent));
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var saved = messageService.sendMessage(senderId, recipientId, conversationId,
                "Reply", "TEXT", null, null, parentId);

        assertEquals(parentId, saved.getReplyToMessageId());
        assertEquals("Original message", saved.getReplyToContent());
        assertEquals(recipientId, saved.getReplyToSenderId());
    }

    @Test
    void usersCannotReactToMessagesFromAnotherConversation() {
        UUID userId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        UUID otherConversationId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();
        Conversation conversation = Conversation.builder()
                .id(conversationId)
                .participantIds(java.util.List.of(userId, UUID.randomUUID()))
                .isGroup(false)
                .build();
        Message message = Message.builder()
                .id(messageId)
                .conversationId(otherConversationId)
                .build();
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(messageRepository.findById(messageId)).thenReturn(Optional.of(message));

        assertThrows(
                IllegalArgumentException.class,
                () -> messageService.toggleReaction(userId, conversationId, messageId, "❤️"));

        verify(messageReactionRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void unsupportedMessageReactionsAreRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> messageService.toggleReaction(
                        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "🚀"));
    }

    @Test
    void participantsCanAddAReactionToConversationMessages() {
        UUID userId = UUID.randomUUID();
        UUID partnerId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();
        Conversation conversation = Conversation.builder()
                .id(conversationId)
                .participantIds(java.util.List.of(userId, partnerId))
                .isGroup(false)
                .build();
        Message message = Message.builder()
                .id(messageId)
                .conversationId(conversationId)
                .senderId(partnerId)
                .receiverId(userId)
                .status(Message.MessageStatus.SENT)
                .type(Message.MessageType.TEXT)
                .build();
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(messageRepository.findById(messageId)).thenReturn(Optional.of(message));
        when(messageReactionRepository.findByMessageIdAndUserIdAndEmoji(messageId, userId, "🐾"))
                .thenReturn(Optional.empty());
        when(messageReactionRepository.save(any(com.petconnect.messages.domain.model.MessageReaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(messageReactionRepository.findByMessageId(messageId))
                .thenReturn(java.util.List.of(new com.petconnect.messages.domain.model.MessageReaction(
                        messageId, userId, "🐾")));

        var updated = messageService.toggleReaction(userId, conversationId, messageId, "🐾");

        assertEquals(1, updated.getReactions().size());
        assertEquals("🐾", updated.getReactions().get(0).emoji());
        assertEquals(userId, updated.getReactions().get(0).userId());
        verify(messageReactionRepository).save(any());
    }

    private static UserProfile profile(UUID authUserId) {
        UserProfile profile = new UserProfile(authUserId, "Test", "User", "user");
        profile.setId(UUID.randomUUID());
        return profile;
    }
}
