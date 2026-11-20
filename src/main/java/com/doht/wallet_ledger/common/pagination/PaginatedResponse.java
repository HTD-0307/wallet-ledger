package com.doht.wallet_ledger.common.pagination;

import java.util.List;
import java.util.Objects;
import org.springframework.data.domain.Page;

/**
 * DTO kết quả phân trang chuẩn hóa (số trang bắt đầu từ 1).
 */
public record PaginatedResponse<T>(
        List<T> items,
        int pageNumber,
        int pageSize,
        long totalCount,
        int totalPages,
        boolean hasPreviousPage,
        boolean hasNextPage
) {

    public PaginatedResponse {
        items = (items == null) ? List.of() : List.copyOf(items);
    }

    public static <T> PaginatedResponse<T> from(Page<T> page) {
        Objects.requireNonNull(page, "page must not be null");
        return new PaginatedResponse<>(
                page.getContent(),
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasPrevious(),
                page.hasNext()
        );
    }

    public static <T> PaginatedResponse<T> of(List<T> items, long totalCount, int pageNumber, int pageSize) {
        if (pageNumber < 1) {
            throw new IllegalArgumentException("pageNumber must be >= 1");
        }
        if (pageSize < 1) {
            throw new IllegalArgumentException("pageSize must be >= 1");
        }
        if (totalCount < 0) {
            throw new IllegalArgumentException("totalCount must be >= 0");
        }

        int totalPages = totalCount == 0 ? 0 : (int) Math.ceil((double) totalCount / pageSize);
        boolean hasPreviousPage = pageNumber > 1 && totalPages > 0;
        boolean hasNextPage = pageNumber < totalPages;

        return new PaginatedResponse<>(
                items,
                pageNumber,
                pageSize,
                totalCount,
                totalPages,
                hasPreviousPage,
                hasNextPage
        );
    }
}
