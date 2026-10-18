package com.petconnect.home.presentation.rest;

import com.petconnect.home.application.HomeService;
import com.petconnect.home.application.dto.AdoptionResponse;
import com.petconnect.home.domain.Suggestion;
import com.petconnect.home.domain.MarketplacePromo;
import com.petconnect.home.domain.VetService;
import com.petconnect.home.domain.Achievement;
import com.petconnect.home.domain.UserLevel;
import com.petconnect.shared.presentation.dto.PageResponse;
import com.petconnect.social.domain.Story;
import com.petconnect.shared.infrastructure.security.CurrentUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/home")
public class HomeController {

    private static final Logger log = LoggerFactory.getLogger(HomeController.class);

    private final HomeService homeService;
    private final CurrentUserService currentUserService;

    public HomeController(HomeService homeService, CurrentUserService currentUserService) {
        this.homeService = homeService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/stories")
    public ResponseEntity<List<Story>> getStories() {
        log.debug("GET /api/v1/home/stories");
        return ResponseEntity.ok(homeService.getStories());
    }

    @GetMapping("/suggestions")
    public ResponseEntity<List<Suggestion>> getSuggestions() {
        log.debug("GET /api/v1/home/suggestions");
        return ResponseEntity.ok(homeService.getSuggestions());
    }

    @GetMapping("/market-promos")
    public ResponseEntity<List<MarketplacePromo>> getMarketPromos() {
        log.debug("GET /api/v1/home/market-promos");
        return ResponseEntity.ok(homeService.getMarketPromos());
    }

    @GetMapping("/vet-services")
    public ResponseEntity<List<VetService>> getVetServices() {
        log.debug("GET /api/v1/home/vet-services");
        return ResponseEntity.ok(homeService.getVetServices());
    }

    /** Adopciones disponibles, paginadas por fecha de creación descendente. */
    @GetMapping("/adoptions")
    public ResponseEntity<PageResponse<AdoptionResponse>> getNewAdoptions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.debug("GET /api/v1/home/adoptions page={} size={}", page, size);
        return ResponseEntity.ok(homeService.getNewAdoptions(page, size));
    }

    @GetMapping("/achievements")
    public ResponseEntity<List<Achievement>> getUpcomingAchievements() {
        log.debug("GET /api/v1/home/achievements");
        UUID userId = currentUserService.getCurrentUserId().orElse(null);
        return ResponseEntity.ok(homeService.getUpcomingAchievements(userId));
    }

    @GetMapping("/user-level")
    public ResponseEntity<UserLevel> getUserLevel() {
        log.debug("GET /api/v1/home/user-level");
        UUID userId = currentUserService.getCurrentUserId().orElse(null);
        return ResponseEntity.ok(homeService.getUserLevel(userId));
    }
}