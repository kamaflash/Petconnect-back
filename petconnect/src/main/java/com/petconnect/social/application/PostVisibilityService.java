package com.petconnect.social.application;

import com.petconnect.social.domain.Post;
import com.petconnect.social.domain.PostAudience;
import com.petconnect.social.domain.repositories.FollowRepository;
import com.petconnect.users.application.BlockedUserService;
import com.petconnect.users.domain.UserProfile;
import com.petconnect.users.domain.repositories.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PostVisibilityService {

    private final UserProfileRepository userProfileRepository;
    private final FollowRepository followRepository;
    private final BlockedUserService blockedUserService;

    public PostVisibilityService(
            UserProfileRepository userProfileRepository,
            FollowRepository followRepository,
            BlockedUserService blockedUserService) {
        this.userProfileRepository = userProfileRepository;
        this.followRepository = followRepository;
        this.blockedUserService = blockedUserService;
    }

    @Transactional(readOnly = true)
    public boolean canView(Post post, UUID viewerAuthUserId) {
        var author = userProfileRepository.findById(post.getAuthorId()).orElse(null);
        if (author == null) {
            return false;
        }
        if (viewerAuthUserId == null) {
            return author.isProfilePublic() && post.getAudience() == PostAudience.PUBLIC;
        }
        if (viewerAuthUserId.equals(author.getAuthUserId())) {
            return true;
        }
        if (blockedUserService.isBlocked(viewerAuthUserId, author.getAuthUserId())) {
            return false;
        }

        var viewer = userProfileRepository.findByAuthUserId(viewerAuthUserId).orElse(null);
        boolean followsAuthor = viewer != null
                && followRepository.existsByFollowerIdAndFollowingId(viewer.getId(), author.getId());
        if (!author.isProfilePublic() && !followsAuthor) {
            return false;
        }

        return switch (post.getAudience()) {
            case PUBLIC -> author.isProfilePublic() || followsAuthor;
            case FOLLOWERS -> followsAuthor;
            case ONLY_ME -> false;
        };
    }

    @Transactional(readOnly = true)
    public boolean canViewProfile(UserProfile profile, UUID viewerAuthUserId) {
        if (viewerAuthUserId == null) {
            return profile.isProfilePublic();
        }
        if (viewerAuthUserId.equals(profile.getAuthUserId())) {
            return true;
        }
        if (blockedUserService.isBlocked(viewerAuthUserId, profile.getAuthUserId())) {
            return false;
        }
        if (profile.isProfilePublic()) {
            return true;
        }
        var viewer = userProfileRepository.findByAuthUserId(viewerAuthUserId).orElse(null);
        return viewer != null
                && followRepository.existsByFollowerIdAndFollowingId(viewer.getId(), profile.getId());
    }
}
