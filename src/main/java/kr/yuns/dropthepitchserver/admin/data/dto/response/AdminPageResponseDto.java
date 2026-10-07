package kr.yuns.dropthepitchserver.admin.data.dto.response;

import java.util.List;

public record AdminPageResponseDto<T>(
        List<T> items,
        int page,
        int size,
        long totalCount,
        int totalPages,
        boolean hasNext
) {
    public static <T> AdminPageResponseDto<T> of(List<T> items, int page, int size, long totalCount) {
        int totalPages = (int) Math.ceil((double) totalCount / size);
        return new AdminPageResponseDto<>(items, page, size, totalCount, totalPages,
                (long) (page + 1) * size < totalCount);
    }

    public static <T> AdminPageResponseDto<T> slice(List<T> all, int page, int size) {
        List<T> items = all.stream()
                .skip((long) page * size)
                .limit(size)
                .toList();
        return of(items, page, size, all.size());
    }
}
