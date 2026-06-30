package com.localmind.shared.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PageRequestTest {

    @Test
    void testDefaults() {
        PageRequest request = new PageRequest();

        assertThat(request.getPage()).isZero();
        assertThat(request.getSize()).isEqualTo(20);
        assertThat(request.getSortBy()).isEqualTo("createdAt");
        assertThat(request.getSortDirection()).isEqualTo("DESC");
    }

    @Test
    void testCustomValues() {
        PageRequest request = new PageRequest(3, 50, "title", "ASC");

        assertThat(request.getPage()).isEqualTo(3);
        assertThat(request.getSize()).isEqualTo(50);
        assertThat(request.getSortBy()).isEqualTo("title");
        assertThat(request.getSortDirection()).isEqualTo("ASC");
    }
}
