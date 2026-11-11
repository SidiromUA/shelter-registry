package ua.edu.ldubgd.shelters.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/** Сторінка результатів для API: дані + інформація про пагінацію. */
public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }
}
