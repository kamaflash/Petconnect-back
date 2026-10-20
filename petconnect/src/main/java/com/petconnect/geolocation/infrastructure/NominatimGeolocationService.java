package com.petconnect.geolocation.infrastructure;

import com.petconnect.geolocation.application.GeolocationService;
import com.petconnect.geolocation.domain.GeocodingResult;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

/**
 * Implementación de {@link GeolocationService} que llama a Nominatim
 * (OpenStreetMap) desde el servidor, evitando problemas de CORS desde el frontend.
 */
@Service
public class NominatimGeolocationService implements GeolocationService {

    private static final Logger log = LoggerFactory.getLogger(NominatimGeolocationService.class);

    private static final String USER_AGENT = "PetConnect-App/1.0 (contact@petconnect.app)";

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final ObjectMapper objectMapper;

    public NominatimGeolocationService(
            RestTemplate restTemplate,
            @Value("${nominatim.base-url:https://nominatim.openstreetmap.org}") String baseUrl,
            ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<GeocodingResult> search(String q, int limit) {
        if (q == null || q.trim().isEmpty()) {
            return List.of();
        }

        try {
            String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/search")
                    .queryParam("q", q)
                    .queryParam("format", "json")
                    .queryParam("limit", String.valueOf(limit))
                    .queryParam("addressdetails", "1")
                    .queryParam("accept-language", "es,en")
                    .build()
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", USER_AGENT);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, org.springframework.http.HttpMethod.GET, entity, String.class);
            String json = response.getBody();

            if (json == null || json.isBlank()) {
                return List.of();
            }

            List<Map<String, Object>> results = objectMapper.readValue(
                    json, new TypeReference<List<Map<String, Object>>>() {});

            return results.stream()
                    .map(this::mapToGeocodingResult)
                    .toList();
        } catch (Exception e) {
            log.error("Error en geocodificación de Nominatim para q={}", q, e);
            return List.of();
        }
    }

    @Override
    public GeocodingResult reverse(double lat, double lon) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/reverse")
                    .queryParam("lat", String.valueOf(lat))
                    .queryParam("lon", String.valueOf(lon))
                    .queryParam("format", "json")
                    .queryParam("addressdetails", "1")
                    .queryParam("accept-language", "es,en")
                    .build()
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", USER_AGENT);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, org.springframework.http.HttpMethod.GET, entity, String.class);
            String json = response.getBody();

            if (json == null || json.isBlank()) {
                return null;
            }

            JsonNode node = objectMapper.readTree(json);
            return mapJsonNodeToGeocodingResult(node);
        } catch (Exception e) {
            log.error("Error en geocodificación inversa de Nominatim para lat={}, lon={}", lat, lon, e);
            return null;
        }
    }

    private GeocodingResult mapToGeocodingResult(Map<String, Object> result) {
        double lat = Double.parseDouble(String.valueOf(result.getOrDefault("lat", "0")));
        double lon = Double.parseDouble(String.valueOf(result.getOrDefault("lon", "0")));
        String displayName = (String) result.getOrDefault("display_name", "");
        @SuppressWarnings("unchecked")
        Map<String, Object> address = (Map<String, Object>) result.getOrDefault("address", Map.of());
        return new GeocodingResult(lat, lon, displayName, address);
    }

    private GeocodingResult mapJsonNodeToGeocodingResult(JsonNode node) {
        double lat = node.has("lat") ? node.get("lat").asDouble() : 0.0;
        double lon = node.has("lon") ? node.get("lon").asDouble() : 0.0;
        String displayName = node.has("display_name") ? node.get("display_name").asText() : "";
        Map<String, Object> address = node.has("address")
                ? objectMapper.convertValue(node.get("address"), Map.class)
                : Map.of();
        return new GeocodingResult(lat, lon, displayName, address);
    }
}