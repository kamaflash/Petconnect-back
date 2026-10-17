package com.petconnect.social.domain.repositories;

import com.petconnect.social.domain.CommunityGroupMember;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommunityGroupMemberRepository {

    Optional<CommunityGroupMember> findByGroupIdAndUserId(UUID groupId, UUID userId);

    boolean existsByGroupIdAndUserId(UUID groupId, UUID userId);

    List<CommunityGroupMember> findByUserId(UUID userId);

    List<CommunityGroupMember> findByGroupId(UUID groupId);

    long countByGroupId(UUID groupId);

    CommunityGroupMember save(CommunityGroupMember member);

    void deleteByGroupIdAndUserId(UUID groupId, UUID userId);
}
