package com.doht.wallet_ledger.common.pagination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class PaginatedResponseTest {

    @Test
    void shouldMapFromSpringDataPageWithOneBasedPageNumber() {
        Page<String> page = new PageImpl<>(List.of("tx-3", "tx-4"), PageRequest.of(1, 2), 5);

        PaginatedResponse<String> response = PaginatedResponse.from(page);

        assertThat(response.items()).containsExactly("tx-3", "tx-4");
        assertThat(response.pageNumber()).isEqualTo(2);
        assertThat(response.pageSize()).isEqualTo(2);
        assertThat(response.totalCount()).isEqualTo(5L);
        assertThat(response.totalPages()).isEqualTo(3);
        assertThat(response.hasPreviousPage()).isTrue();
        assertThat(response.hasNextPage()).isTrue();
    }

    @Test
    void shouldHandleEmptyPageCorrectly() {
        PaginatedResponse<String> response = PaginatedResponse.of(null, 0, 1, 10);

        assertThat(response.items()).isEmpty();
        assertThat(response.totalCount()).isZero();
        assertThat(response.totalPages()).isZero();
        assertThat(response.hasPreviousPage()).isFalse();
        assertThat(response.hasNextPage()).isFalse();
    }

    @Test
    void shouldRejectInvalidPaginationArguments() {
        assertThatThrownBy(() -> PaginatedResponse.of(List.of(), 0, 0, 10))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PaginatedResponse.of(List.of(), 0, 1, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PaginatedResponse.of(List.of(), -1, 1, 10))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
