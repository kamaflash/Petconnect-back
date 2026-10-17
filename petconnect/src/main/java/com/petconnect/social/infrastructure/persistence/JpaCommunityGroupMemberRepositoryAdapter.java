package com.petconnect.social.infrastructure.persistence;

import com.petconnect.social.domain.CommunityGroupMember;
import com.petconnect.social.domain.repositories.CommunityGroupMemberRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class JpaCommunityGroupMemberRepositoryAdapter implements CommunityGroupMemberRepository {

    private final SpringDataCommunityGroupMemberRepository springDataRepository;

    public JpaCommunityGroupMemberRepositoryAdapter(
            SpringDataCommunityGroupMemberRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public Optional<CommunityGroupMember> findByGroupIdAndUserId(UUID groupId, UUID userId) {
        return springDataRepository.findByGroupIdAndUserId(groupId, userId);
    }

    @Override
    public boolean existsByGroupIdAndUserId(UUID groupId, UUID userId) {
        return springDataRepository.existsByGroupIdAndUserId(groupId, userId);
    }

    @Override
    public List<CommunityGroupMember> findByUserId(UUID userId) {
        return springDataRepository.findByUserId(userId);
    }

    @Override
    public List<CommunityGroupMember> findByGroupId(UUID groupId) {
        return springDataRepository.findByGroupId(groupId);
    }

    @Override
    public long countByGroupId(UUID groupId) {
        return springDataRepository.countByGroupId(groupId);
    }

    @Override
    public CommunityGroupMember save(CommunityGroupMember member) {
        return springDataRepository.save(member);
    }

    @Override
    public void deleteByGroupIdAndUserId(UUID groupId, UUID userId) {
        springDataRepository.deleteByGroupIdAndUserId(groupId, userId);
    }
}
