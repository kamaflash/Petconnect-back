package com.petconnect.social.application;

import com.petconnect.social.domain.Post;
import com.petconnect.social.domain.PostAudience;
import com.petconnect.social.domain.repositories.FollowRepository;
import com.petconnect.users.application.BlockedUserService;
import com.petconnect.users.domain.UserProfile;
import com.petconnect.users.domain.repositories.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PostVisibilityServiceTest {

    private final UserProfileRepository userProfileRepository = mock(UserProfileRepository.class);
    private final FollowRepository followRepository = mock(FollowRepository.class);
    private final BlockedUserService blockedUserService = mock(BlockedUserService.class);
    private final UUID authorAuthId = UUID.randomUUID();
    private final UUID viewerAuthId = UUID.randomUUID();
    private final UserProfile author = new UserProfile(authorAuthId, "Author", "User", "owner");
    private final UserProfile viewer = new UserProfile(viewerAuthId, "Viewer", "User", "owner");
    private final PostVisibilityService service = new PostVisibilityService(
            userProfileRepository, followRepository, blockedUserService);

    @BeforeEach
    void setUp() {
        author.setProfilePublic(false);
        when(userProfileRepository.findById(author.getId())).thenReturn(Optional.of(author));
        when(userProfileRepository.findByAuthUserId(viewerAuthId)).thenReturn(Optional.of(viewer));
        when(blockedUserService.isBlocked(viewerAuthId, authorAuthId)).thenReturn(false);
    }

    @Test
    void privateProfileAndPostAreVisibleToFollowers() {
        when(followRepository.existsByFollowerIdAndFollowingId(viewer.getId(), author.getId()))
                .thenReturn(true);
        Post post = postWithAudience(PostAudience.PUBLIC);

        assertTrue(service.canViewProfile(author, viewerAuthId));
        assertTrue(service.canView(post, viewerAuthId));
    }

    @Test
    void privateProfileAndPostAreHiddenFromNonFollowersAndAnonymousVisitors() {
        when(followRepository.existsByFollowerIdAndFollowingId(viewer.getId(), author.getId()))
                .thenReturn(false);

        assertFalse(service.canViewProfile(author, viewerAuthId));
        assertFalse(service.canViewProfile(author, null));
        assertFalse(service.canView(postWithAudience(PostAudience.PUBLIC), viewerAuthId));
        assertFalse(service.canView(postWithAudience(PostAudience.PUBLIC), null));
    }

    @Test
    void postAudienceRestrictsVisibilityEvenForFollowers() {
        when(followRepository.existsByFollowerIdAndFollowingId(viewer.getId(), author.getId()))
                .thenReturn(true);

        assertTrue(service.canView(postWithAudience(PostAudience.FOLLOWERS), viewerAuthId));
        assertFalse(service.canView(postWithAudience(PostAudience.ONLY_ME), viewerAuthId));
        assertFalse(service.canView(postWithAudience(PostAudience.ONLY_ME), null));
    }

    @Test
    void authorCanAlwaysViewTheirOwnPosts() {
        assertTrue(service.canView(postWithAudience(PostAudience.ONLY_ME), authorAuthId));
        assertTrue(service.canViewProfile(author, authorAuthId));
    }

    private Post postWithAudience(PostAudience audience) {
        Post post = new Post(author.getId(), "", "caption");
        post.setAudience(audience);
        return post;
    }
}
