package com.petconnect.social.infrastructure.persistence;

import com.petconnect.social.domain.Post;
import com.petconnect.social.domain.PostIntent;
import com.petconnect.users.domain.UserProfile;
import com.petconnect.social.domain.Follow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SpringDataPostRepository extends JpaRepository<Post, UUID> {
    List<Post> findByAuthorId(UUID authorId);

    List<Post> findByAuthorIdAndActiveTrue(UUID authorId);

    List<Post> findByAuthorIdIn(List<UUID> authorIds);

    Page<Post> findByActiveTrue(Pageable pageable);

    Page<Post> findByActiveTrueAndAuthorIdNotIn(List<UUID> excludedAuthorIds, Pageable pageable);

    Page<Post> findByAuthorIdInAndActiveTrue(List<UUID> authorIds, Pageable pageable);

    List<Post> findByPetId(UUID petId);

    List<Post> findByPetIdAndActiveTrue(UUID petId);

    Page<Post> findByPetIdAndActiveTrue(UUID petId, Pageable pageable);

    /** Posts activos de una intencion concreta. */
    Page<Post> findByActiveTrueAndIntent(PostIntent intent, Pageable pageable);

    Page<Post> findByActiveTrueAndIntentAndAuthorIdNotIn(
            PostIntent intent, List<UUID> excludedAuthorIds, Pageable pageable);

    /** Posts activos de los autores indicados y de una intencion concreta. */
    Page<Post> findByActiveTrueAndAuthorIdInAndIntent(List<UUID> authorIds, PostIntent intent, Pageable pageable);

    Page<Post> findByActiveTrueAndAuthorIdInAndAuthorIdNotIn(
            List<UUID> authorIds, List<UUID> excludedAuthorIds, Pageable pageable);

    Page<Post> findByActiveTrueAndAuthorIdInAndIntentAndAuthorIdNotIn(
            List<UUID> authorIds, PostIntent intent, List<UUID> excludedAuthorIds, Pageable pageable);

    @Query(
            value = """
                    select post
                    from Post post
                    where post.active = true
                      and (:intent is null or post.intent = :intent)
                      and (:excludeBlockedAuthors = false or post.authorId not in :excludedAuthorProfileIds)
                      and (:filterAuthors = false or post.authorId in :authorProfileIds)
                      and (
                          (:viewerProfileId is not null and post.authorId = :viewerProfileId)
                          or exists (
                              select author.id
                              from UserProfile author
                              where author.id = post.authorId
                                and (
                                    (author.profilePublic = true and post.audience = com.petconnect.social.domain.PostAudience.PUBLIC)
                                    or (
                                        post.audience in (
                                            com.petconnect.social.domain.PostAudience.PUBLIC,
                                            com.petconnect.social.domain.PostAudience.FOLLOWERS
                                        )
                                        and :viewerProfileId is not null
                                        and exists (
                                            select follow.id
                                            from Follow follow
                                            where follow.followerId = :viewerProfileId
                                              and follow.followingId = author.id
                                        )
                                    )
                                )
                          )
                      )
                    """,
            countQuery = """
                    select count(post)
                    from Post post
                    where post.active = true
                      and (:intent is null or post.intent = :intent)
                      and (:excludeBlockedAuthors = false or post.authorId not in :excludedAuthorProfileIds)
                      and (:filterAuthors = false or post.authorId in :authorProfileIds)
                      and (
                          (:viewerProfileId is not null and post.authorId = :viewerProfileId)
                          or exists (
                              select author.id
                              from UserProfile author
                              where author.id = post.authorId
                                and (
                                    (author.profilePublic = true and post.audience = com.petconnect.social.domain.PostAudience.PUBLIC)
                                    or (
                                        post.audience in (
                                            com.petconnect.social.domain.PostAudience.PUBLIC,
                                            com.petconnect.social.domain.PostAudience.FOLLOWERS
                                        )
                                        and :viewerProfileId is not null
                                        and exists (
                                            select follow.id
                                            from Follow follow
                                            where follow.followerId = :viewerProfileId
                                              and follow.followingId = author.id
                                        )
                                    )
                                )
                          )
                      )
                    """)
    Page<Post> findVisibleActive(
            @Param("intent") PostIntent intent,
            @Param("viewerProfileId") UUID viewerProfileId,
            @Param("excludedAuthorProfileIds") List<UUID> excludedAuthorProfileIds,
            @Param("excludeBlockedAuthors") boolean excludeBlockedAuthors,
            @Param("authorProfileIds") List<UUID> authorProfileIds,
            @Param("filterAuthors") boolean filterAuthors,
            Pageable pageable);
}
