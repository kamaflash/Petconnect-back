package com.petconnect.home.presentation.rest;

import com.petconnect.home.application.HomeService;
import com.petconnect.home.domain.Story;
import com.petconnect.home.domain.Suggestion;
import com.petconnect.home.domain.MarketplacePromo;
import com.petconnect.home.domain.VetService;
import com.petconnect.home.domain.Adoption;
import com.petconnect.home.domain.Achievement;
import com.petconnect.home.domain.UserLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/home")
public class HomeController {

    private static final Logger log = LoggerFactory.getLogger(HomeController.class);

    private final HomeService homeService;

    public HomeController(HomeService homeService) {
        this.homeService = homeService;
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

    @GetMapping("/adoptions")
    public ResponseEntity<List<Adoption>> getNewAdoptions() {
        log.debug("GET /api/v1/home/adoptions");
        return ResponseEntity.ok(homeService.getNewAdoptions());
    }

    @GetMapping("/achievements")
    public ResponseEntity<List<Achievement>> getUpcomingAchievements() {
        log.debug("GET /api/v1/home/achievements");
        return ResponseEntity.ok(homeService.getUpcomingAchievements());
    }

    @GetMapping("/user-level")
    public ResponseEntity<UserLevel> getUserLevel() {
        log.debug("GET /api/v1/home/user-level");
        return ResponseEntity.ok(homeService.getUserLevel());
    }
}