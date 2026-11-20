package com.doht.wallet_ledger.common.pagination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.doht.wallet_ledger.common.exception.InvalidSortFieldException;
import com.doht.wallet_ledger.common.i18n.MessageKeys;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

class PageQueryTest {

    @Test
    void shouldNormalizeNullAndOutOfBoundsInputs() {
        PageQuery defaults = new PageQuery(null, null, "   ", " ", null);
        assertThat(defaults.pageNumber()).isEqualTo(1);
        assertThat(defaults.pageSize()).isEqualTo(10);
        assertThat(defaults.searchTerm()).isNull();
        assertThat(defaults.sortBy()).isNull();
        assertThat(defaults.sortDescending()).isFalse();

        PageQuery clamped = new PageQuery(-5, 999, "  wallet  ", " createdAt ", true);
        assertThat(clamped.pageNumber()).isEqualTo(1);
        assertThat(clamped.pageSize()).isEqualTo(PageQuery.MAX_PAGE_SIZE);
        assertThat(clamped.searchTerm()).isEqualTo("wallet");
        assertThat(clamped.sortBy()).isEqualTo("createdAt");
        assertThat(clamped.sortDescending()).isTrue();
    }

    @Test
    void shouldConvertOneBasedPageNumberToZeroBasedPageable() {
        PageQuery query = new PageQuery(3, 25, null, "amount", true);

        Pageable pageable = query.toPageable(Set.of("createdAt", "amount"), "createdAt");

        assertThat(pageable.getPageNumber()).isEqualTo(2);
        assertThat(pageable.getPageSize()).isEqualTo(25);
        Sort.Order order = pageable.getSort().getOrderFor("amount");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void shouldUseDefaultSortFieldWhenSortByIsNull() {
        PageQuery query = PageQuery.defaults();

        Pageable pageable = query.toPageable(Set.of("createdAt", "amount"), "createdAt");

        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(10);
        Sort.Order order = pageable.getSort().getOrderFor("createdAt");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void shouldRejectDisallowedSortField() {
        PageQuery query = new PageQuery(1, 10, null, "passwordHash", false);
        Set<String> allowedSortFields = Set.of("createdAt", "amount");

        assertThatThrownBy(() -> query.toPageable(allowedSortFields, "createdAt"))
                .isInstanceOf(InvalidSortFieldException.class)
                .satisfies(ex -> {
                    InvalidSortFieldException sortEx = (InvalidSortFieldException) ex;
                    assertThat(sortEx.getMessageKey()).isEqualTo(MessageKeys.COMMON_ERROR_INVALID_SORT_FIELD);
                    assertThat(sortEx.getMessageArgs()).contains("passwordHash");
                });
    }
}
