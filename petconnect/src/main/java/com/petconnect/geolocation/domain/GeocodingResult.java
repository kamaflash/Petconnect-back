package com.petconnect.geolocation.domain;

import java.util.Map;

/**
 * Resultado de geocodificación Nominatim.
 */
public record GeocodingResult(
        double lat,
        double lon,
        String displayName,
        Map<String, Object> address
) {
}