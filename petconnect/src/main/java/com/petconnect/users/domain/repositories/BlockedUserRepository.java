package com.petconnect.users.domain.repositories;

import com.petconnect.users.domain.BlockedUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BlockedUserRepository extends JpaRepository<BlockedUser, UUID> {

    List<BlockedUser> findByBlockerAuthUserIdOrderByCreatedAtDesc(UUID blockerAuthUserId);

    List<BlockedUser> findByBlockedAuthUserId(UUID blockedAuthUserId);

    Optional<BlockedUser> findByBlockerAuthUserIdAndBlockedAuthUserId(
            UUID blockerAuthUserId, UUID blockedAuthUserId);

    boolean existsByBlockerAuthUserIdAndBlockedAuthUserId(
            UUID blockerAuthUserId, UUID blockedAuthUserId);

    @Query("""
            select count(blocked) > 0 from BlockedUser blocked
            where (blocked.blockerAuthUserId = :firstUserId and blocked.blockedAuthUserId = :secondUserId)
               or (blocked.blockerAuthUserId = :secondUserId and blocked.blockedAuthUserId = :firstUserId)
            """)
    boolean existsBlockBetween(
            @Param("firstUserId") UUID firstUserId,
            @Param("secondUserId") UUID secondUserId);

    void deleteByBlockerAuthUserIdAndBlockedAuthUserId(
            UUID blockerAuthUserId, UUID blockedAuthUserId);
}
