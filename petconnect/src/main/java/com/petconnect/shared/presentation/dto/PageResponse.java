package com.petconnect.shared.presentation.dto;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Envoltura genérica de respuesta paginada para los endpoints de listado.
 * Permite a los consumidores (p. ej. table-card con scroll infinito) cargar
 * resultados de 10 en 10 sin romper aplicaciones que usan la lista completa.
 *
 * @param <T> tipo de los elementos contenidos
 */
public record PageResponse<T>(
        List<T> content,
        long totalElements,
        int totalPages,
        int page,
        int size,
        boolean last) {

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize(),
                page.isLast());
    }

    /** Convierte el contenido de una página a otro tipo mapeando cada elemento. */
    public static <S, T> PageResponse<T> from(Page<S> page, Function<S, T> mapper) {
        return new PageResponse<>(
                page.getContent().stream().map(mapper).toList(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize(),
                page.isLast());
    }
}