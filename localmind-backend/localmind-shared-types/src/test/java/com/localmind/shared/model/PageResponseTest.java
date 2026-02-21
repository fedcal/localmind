package com.localmind.shared.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageResponseTest {

    @Test
    void testBuilder_setsAllFields() {
        List<String> content = List.of("a", "b", "c");

        PageResponse<String> response = PageResponse.<String>builder()
                .content(content)
                .page(2)
                .size(10)
                .totalElements(53)
                .totalPages(6)
                .first(false)
                .last(false)
                .build();

        assertThat(response.getContent()).containsExactly("a", "b", "c");
        assertThat(response.getPage()).isEqualTo(2);
        assertThat(response.getSize()).isEqualTo(10);
        assertThat(response.getTotalElements()).isEqualTo(53);
        assertThat(response.getTotalPages()).isEqualTo(6);
        assertThat(response.isFirst()).isFalse();
        assertThat(response.isLast()).isFalse();
    }

    @Test
    void testPagination_firstPage() {
        PageResponse<Integer> response = PageResponse.<Integer>builder()
                .content(List.of(1, 2, 3))
                .page(0)
                .size(3)
                .totalElements(10)
                .totalPages(4)
                .first(true)
                .last(false)
                .build();

        assertThat(response.getPage()).isZero();
        assertThat(response.isFirst()).isTrue();
        assertThat(response.isLast()).isFalse();
        assertThat(response.getTotalPages()).isEqualTo(4);
    }

    @Test
    void testPagination_lastPage() {
        PageResponse<Integer> response = PageResponse.<Integer>builder()
                .content(List.of(10))
                .page(3)
                .size(3)
                .totalElements(10)
                .totalPages(4)
                .first(false)
                .last(true)
                .build();

        assertThat(response.getPage()).isEqualTo(3);
        assertThat(response.isFirst()).isFalse();
        assertThat(response.isLast()).isTrue();
        assertThat(response.getContent()).hasSize(1);
    }
}
