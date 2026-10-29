package application.domain.valueobjects;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Domain-agnostic pagination result container.
 */
public record PageResult<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {
    public PageResult {
        content = content != null ? List.copyOf(content) : Collections.emptyList();
    }

    public static <T> PageResult<T> of(List<T> allItems, int page, int size) {
        List<T> safeList = allItems != null ? allItems : Collections.emptyList();
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, size);
        long totalElements = safeList.size();
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / safeSize);

        int fromIndex = safePage * safeSize;
        List<T> slice;
        if (fromIndex >= safeList.size()) {
            slice = Collections.emptyList();
        } else {
            int toIndex = Math.min(fromIndex + safeSize, safeList.size());
            slice = safeList.subList(fromIndex, toIndex);
        }

        boolean isLast = totalPages == 0 || safePage >= totalPages - 1;
        return new PageResult<>(slice, safePage, safeSize, totalElements, totalPages, isLast);
    }

    public <R> PageResult<R> map(Function<T, R> mapper) {
        List<R> mapped = content.stream().map(mapper).toList();
        return new PageResult<>(mapped, page, size, totalElements, totalPages, last);
    }
}
