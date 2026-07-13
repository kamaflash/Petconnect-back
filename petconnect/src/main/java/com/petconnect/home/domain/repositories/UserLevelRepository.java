package com.petconnect.home.domain.repositories;

import com.petconnect.home.domain.UserLevel;
import java.util.Optional;

public interface UserLevelRepository {
    Optional<UserLevel> findFirstByOrderByIdDesc();
}
