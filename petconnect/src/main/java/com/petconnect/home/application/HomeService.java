package com.petconnect.home.application;

import com.petconnect.home.domain.*;
import com.petconnect.social.domain.Story;
import com.petconnect.social.domain.repositories.StoryRepository;
import com.petconnect.home.domain.repositories.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HomeService {

        private final StoryRepository storyRepository;
        private final SuggestionRepository suggestionRepository;
        private final MarketplacePromoRepository marketPromoRepository;
        private final VetServiceRepository vetServiceRepository;
        private final AdoptionRepository adoptionRepository;
        private final AchievementRepository achievementRepository;
        private final UserLevelRepository userLevelRepository;

        public HomeService(StoryRepository storyRepository,
                           SuggestionRepository suggestionRepository,
                           MarketplacePromoRepository marketPromoRepository,
                           VetServiceRepository vetServiceRepository,
                           AdoptionRepository adoptionRepository,
                           AchievementRepository achievementRepository,
                           UserLevelRepository userLevelRepository) {
                this.storyRepository = storyRepository;
                this.suggestionRepository = suggestionRepository;
                this.marketPromoRepository = marketPromoRepository;
                this.vetServiceRepository = vetServiceRepository;
                this.adoptionRepository = adoptionRepository;
                this.achievementRepository = achievementRepository;
                this.userLevelRepository = userLevelRepository;
        }

        public List<Story> getStories() {
                return storyRepository.findActiveStoriesOrderByCreatedAt();
        }

        public List<Suggestion> getSuggestions() {
                return suggestionRepository.findTop4ByOrderByCreatedAtDesc();
        }

        public List<MarketplacePromo> getMarketPromos() {
                return marketPromoRepository.findByActiveTrue();
        }

        public List<VetService> getVetServices() {
                return vetServiceRepository.findByActiveTrue();
        }

        public List<Adoption> getNewAdoptions() {
                return adoptionRepository.findByStatus("AVAILABLE");
        }

        public List<Achievement> getUpcomingAchievements() {
                return achievementRepository.findAll();
        }

        public UserLevel getUserLevel() {
                return userLevelRepository.findFirstByOrderByIdDesc()
                        .orElse(new UserLevel(12, 650, 1000));
        }
}
