package com.petconnect.home.application;

import com.petconnect.home.application.dto.AdoptionResponse;
import com.petconnect.home.domain.*;
import com.petconnect.pets.application.dto.PetResponse;
import com.petconnect.pets.domain.repositories.PetRepository;
import com.petconnect.social.domain.Story;
import com.petconnect.social.domain.repositories.StoryRepository;
import com.petconnect.home.domain.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class HomeService {

        private final StoryRepository storyRepository;
        private final SuggestionRepository suggestionRepository;
        private final MarketplacePromoRepository marketPromoRepository;
        private final VetServiceRepository vetServiceRepository;
        private final AdoptionRepository adoptionRepository;
        private final AchievementRepository achievementRepository;
        private final UserLevelRepository userLevelRepository;
        private final PetRepository petRepository;

        public HomeService(StoryRepository storyRepository,
                        SuggestionRepository suggestionRepository,
                        MarketplacePromoRepository marketPromoRepository,
                        VetServiceRepository vetServiceRepository,
                        AdoptionRepository adoptionRepository,
                        AchievementRepository achievementRepository,
                        UserLevelRepository userLevelRepository,
                        PetRepository petRepository) {
                this.storyRepository = storyRepository;
                this.suggestionRepository = suggestionRepository;
                this.marketPromoRepository = marketPromoRepository;
                this.vetServiceRepository = vetServiceRepository;
                this.adoptionRepository = adoptionRepository;
                this.achievementRepository = achievementRepository;
                this.userLevelRepository = userLevelRepository;
                this.petRepository = petRepository;
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

        public List<AdoptionResponse> getNewAdoptions() {
                List<Adoption> adoptions = adoptionRepository.findByStatus("AVAILABLE");
                return adoptions.stream()
                                .map(adoption -> {
                                        var pet = petRepository.findById(adoption.getPetId());
                                        PetResponse petResponse = pet.map(p -> new PetResponse(
                                                        p.getId(),
                                                        p.getOwnerId(),
                                                        p.getName(),
                                                        p.getSpecies().name(),
                                                        p.getBreed(),
                                                        p.getDateOfBirth(),
                                                        p.getGender(),
                                                        p.getBio(),
                                                        p.getAvatarUrl(),
                                                        p.getImageUrls(),
                                                        p.getWeight(),
                                                        p.getWeightUnit(),
                                                        p.isActive(),
                                                        p.getMicrochipId(),
                                                        p.getColor(),
                                                        p.getBloodType(),
                                                        p.getAllergies(),
                                                        p.getMedicalConditions(),
                                                        p.getVetName(),
                                                        p.getVetPhone(),
                                                        p.getVetAddress(),
                                                        p.getLastVaccinationDate(),
                                                        p.getNextVaccinationDate(),
                                                        p.getLastVetVisit(),
                                                        p.getInsuranceProvider(),
                                                        p.getInsurancePolicyNumber(),
                                                        p.getEmergencyContact(),
                                                        p.getEmergencyPhone(),
                                                        p.getAdoptionDate(),
                                                        p.getAdoptionCenter(),
                                                        p.getRegistrationNumber(),
                                                        p.getLicenseNumber(),
                                                        p.getLicenseExpiryDate(),
                                                        p.isSpayedNeutered(),
                                                        p.getSpayedNeuteredDate(),
                                                        p.getTemperament(),
                                                        p.getEnergyLevel(),
                                                        p.getTrainingLevel(),
                                                        p.getFavoriteActivities(),
                                                        p.getFavoriteFood(),
                                                        p.getSpecialNeeds(),
                                                        p.getLastKnownLocation(),
                                                        p.isLost(),
                                                        p.getLostDate(),
                                                        p.isAvailableForAdoption())).orElse(null);
                                        return AdoptionResponse.from(adoption, petResponse);
                                })
                                .toList();
        }

        public List<Achievement> getUpcomingAchievements() {
                return achievementRepository.findAll();
        }

        public UserLevel getUserLevel() {
                return userLevelRepository.findFirstByOrderByIdDesc()
                                .orElse(new UserLevel(12, 650, 1000));
        }
}
