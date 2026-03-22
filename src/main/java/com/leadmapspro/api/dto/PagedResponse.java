package com.leadmapspro.api.dto;

import java.util.List;
import org.springframework.data.domain.Page;

/**
 * JSON estável para listas paginadas (evita serializar {@link org.springframework.data.domain.Page}
 * diretamente — aviso Spring Data sobre {@code PageImpl}).
 */
public record PagedResponse<T>(
        List<T> content, long totalElements, int totalPages, int number, int size) {

    public static <T> PagedResponse<T> from(Page<T> page) {
        return new PagedResponse<>(
                page.getContent(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize());
    }
}
