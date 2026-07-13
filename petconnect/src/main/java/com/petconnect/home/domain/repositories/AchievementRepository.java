package com.petconnect.home.domain.repositories;

import com.petconnect.home.domain.Achievement;
import java.util.List;

public interface AchievementRepository {
    List<Achievement> findAll();
}
