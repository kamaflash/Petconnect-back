package com.petconnect.shared.presentation.rest;

import com.petconnect.shared.infrastructure.services.CloudinaryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Expone un endpoint REST de subida genérica a Cloudinary.
 *
 * El frontend (CloudinaryService) sube ficheros a <code>POST /api/v1/cloudinary/upload</code>,
 * pero hasta ahora ese endpoint no existía en el backend: CloudinaryService solo se inyectaba
 * en casos de uso internos (crear mascota, producto, perfil, posts). Al no haber controller,
 * la petición terminaba en el GlobalExceptionHandler devolviendo un 500.
 */
@RestController
@RequestMapping("/api/v1/cloudinary")
public class CloudController {

    private static final Logger log = LoggerFactory.getLogger(CloudController.class);

    private final Optional<CloudinaryService> cloudinaryService;

    public CloudController(Optional<CloudinaryService> cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No file provided"));
        }

        if (cloudinaryService.isEmpty()) {
            log.warn("Cloudinary no está habilitado (cloudinary.enabled!=true)");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Cloudinary is not enabled"));
        }

        try {
            String url = cloudinaryService.get().uploadImage(file);
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("url", url);
            response.put("secure_url", url);
            response.put("type", "upload");
            log.info("Imagen subida correctamente: {}", url);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            log.error("Error subiendo imagen a Cloudinary", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to upload image: " + e.getMessage()));
        }
    }
}