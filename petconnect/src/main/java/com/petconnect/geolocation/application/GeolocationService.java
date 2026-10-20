package com.petconnect.geolocation.application;

import com.petconnect.geolocation.domain.GeocodingResult;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio de aplicación para geocodificación y geocodificación inversa.
 */
@Service
public interface GeolocationService {

    /**
     * Busca direcciones o lugares por texto (geocodificación).
     *
     * @param q    dirección o nombre de lugar a buscar
     * @param limit número máximo de resultados a devolver
     * @return lista de resultados de geocodificación
     */
    List<GeocodingResult> search(String q, int limit);

    /**
     * Convierte coordenadas en dirección (geocodificación inversa).
     *
     * @param lat latitud
     * @param lon longitud
     * @return resultado de geocodificación inversa
     */
    GeocodingResult reverse(double lat, double lon);
}