package com.petconnect.gamification.domain.repositories;

import com.petconnect.gamification.domain.GoalCatalog;

import java.util.List;

public interface GoalCatalogRepository {

    List<GoalCatalog> findAllActive();
}