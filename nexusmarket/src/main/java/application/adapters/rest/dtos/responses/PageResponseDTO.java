package application.adapters.rest.dtos.responses;

import application.domain.valueobjects.PageResult;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Generic paginated REST response DTO.
 */
public record PageResponseDTO<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {
    public PageResponseDTO {
        content = content != null ? List.copyOf(content) : Collections.emptyList();
    }

    public static <T> PageResponseDTO<T> from(PageResult<T> pageResult) {
        if (pageResult == null) {
            return new PageResponseDTO<>(Collections.emptyList(), 0, 10, 0L, 0, true);
        }
        return new PageResponseDTO<>(
                pageResult.content(),
                pageResult.page(),
                pageResult.size(),
                pageResult.totalElements(),
                pageResult.totalPages(),
                pageResult.last()
        );
    }

    public static <D, T> PageResponseDTO<T> from(PageResult<D> pageResult, Function<D, T> mapper) {
        if (pageResult == null) {
            return new PageResponseDTO<>(Collections.emptyList(), 0, 10, 0L, 0, true);
        }
        List<T> mapped = pageResult.content().stream().map(mapper).toList();
        return new PageResponseDTO<>(
                mapped,
                pageResult.page(),
                pageResult.size(),
                pageResult.totalElements(),
                pageResult.totalPages(),
                pageResult.last()
        );
    }

    public static <T> PageResponseDTO<T> of(List<T> allItems, int page, int size) {
        return from(PageResult.of(allItems, page, size));
    }
}
