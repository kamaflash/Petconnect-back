package com.petconnect.geolocation.presentation.rest;

import com.petconnect.geolocation.application.GeolocationService;
import com.petconnect.geolocation.domain.GeocodingResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para geocodificación.
 * Endpoints intermediarios con Nominatim para evitar problemas de CORS desde el frontend.
 *
 * Endpoints:
 *   GET /api/v1/geocode/search?q=...&limit=...
 *   GET /api/v1/geocode/reverse?lat=...&lon=...
 */
@RestController
@RequestMapping("/api/v1/geocode")
public class GeolocationController {

    private static final Logger log = LoggerFactory.getLogger(GeolocationController.class);

    private final GeolocationService geolocationService;

    public GeolocationController(GeolocationService geolocationService) {
        this.geolocationService = geolocationService;
    }

    /**
     * Busca direcciones o lugares por texto (geocodificación).
     * Reenvía la petición a Nominatim desde el servidor.
     */
    @GetMapping("/search")
    public ResponseEntity<List<GeocodingResult>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "5") int limit) {
        log.debug("GET /api/v1/geocode/search q={} limit={}", q, limit);
        List<GeocodingResult> results = geolocationService.search(q, limit);
        return ResponseEntity.ok(results);
    }

    /**
     * Convierte coordenadas en dirección (geocodificación inversa).
     * Reenvía la petición a Nominatim desde el servidor.
     */
    @GetMapping("/reverse")
    public ResponseEntity<GeocodingResult> reverse(
            @RequestParam double lat,
            @RequestParam double lon) {
        log.debug("GET /api/v1/geocode/reverse lat={} lon={}", lat, lon);
        GeocodingResult result = geolocationService.reverse(lat, lon);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }
}