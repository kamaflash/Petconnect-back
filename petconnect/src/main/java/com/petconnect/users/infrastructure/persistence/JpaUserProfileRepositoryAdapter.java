package com.petconnect.users.infrastructure.persistence;

import com.petconnect.users.domain.UserProfile;
import com.petconnect.users.domain.repositories.UserProfileRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class JpaUserProfileRepositoryAdapter implements UserProfileRepository {

    private final SpringDataUserProfileRepository springDataUserProfileRepository;

    public JpaUserProfileRepositoryAdapter(SpringDataUserProfileRepository springDataUserProfileRepository) {
        this.springDataUserProfileRepository = springDataUserProfileRepository;
    }

    @Override
    public Optional<UserProfile> findById(UUID id) {
        return springDataUserProfileRepository.findById(id);
    }

    @Override
    public Optional<UserProfile> findByAuthUserId(UUID authUserId) {
        return springDataUserProfileRepository.findByAuthUserId(authUserId);
    }

    @Override
    public List<UserProfile> findAll() {
        return springDataUserProfileRepository.findAll();
    }

    @Override
    public UserProfile save(UserProfile userProfile) {
        return springDataUserProfileRepository.save(userProfile);
    }

    @Override
    public void delete(UserProfile userProfile) {
        springDataUserProfileRepository.delete(userProfile);
    }
}