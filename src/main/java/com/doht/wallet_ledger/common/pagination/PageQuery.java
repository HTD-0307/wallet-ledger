package com.doht.wallet_ledger.common.pagination;

import com.doht.wallet_ledger.common.exception.InvalidSortFieldException;
import java.util.Objects;
import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * DTO truy vấn phân trang (bắt đầu từ trang 1) và sắp xếp chuẩn hóa.
 * Tự động giới hạn kích thước trang tối đa và kiểm soát danh sách trường được phép sắp xếp.
 */
public record PageQuery(
        Integer pageNumber,
        Integer pageSize,
        String searchTerm,
        String sortBy,
        Boolean sortDescending
) {

    public static final int DEFAULT_PAGE_NUMBER = 1;
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 100;

    public PageQuery {
        pageNumber = (pageNumber == null || pageNumber < 1) ? DEFAULT_PAGE_NUMBER : pageNumber;
        if (pageSize == null || pageSize < 1) {
            pageSize = DEFAULT_PAGE_SIZE;
        } else if (pageSize > MAX_PAGE_SIZE) {
            pageSize = MAX_PAGE_SIZE;
        }
        searchTerm = (searchTerm == null || searchTerm.isBlank()) ? null : searchTerm.trim();
        sortBy = (sortBy == null || sortBy.isBlank()) ? null : sortBy.trim();
        sortDescending = Boolean.TRUE.equals(sortDescending);
    }

    public static PageQuery defaults() {
        return new PageQuery(DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE, null, null, false);
    }

    public Pageable toPageable(String defaultSortField) {
        Objects.requireNonNull(defaultSortField, "defaultSortField must not be null");
        return toPageable(Set.of(defaultSortField), defaultSortField);
    }

    public Pageable toPageable(Set<String> allowedSortFields, String defaultSortField) {
        Objects.requireNonNull(allowedSortFields, "allowedSortFields must not be null");
        Objects.requireNonNull(defaultSortField, "defaultSortField must not be null");

        String effectiveSortBy = defaultSortField;
        if (sortBy != null) {
            if (!allowedSortFields.contains(sortBy)) {
                throw new InvalidSortFieldException(sortBy, allowedSortFields);
            }
            effectiveSortBy = sortBy;
        }

        Sort.Direction direction = Boolean.TRUE.equals(sortDescending) ? Sort.Direction.DESC : Sort.Direction.ASC;
        int safePage = pageNumber != null ? pageNumber : DEFAULT_PAGE_NUMBER;
        int safeSize = pageSize != null ? pageSize : DEFAULT_PAGE_SIZE;
        return PageRequest.of(safePage - 1, safeSize, Sort.by(direction, effectiveSortBy));
    }
}
