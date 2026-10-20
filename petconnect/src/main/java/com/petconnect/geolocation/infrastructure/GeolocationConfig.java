package com.petconnect.geolocation.infrastructure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuración de infraestructura para geolocalización.
 */
@Configuration
public class GeolocationConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}