package com.localmind.integration;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class FlywayMigrationIT extends AbstractIntegrationTest {

    @Autowired
    private Flyway flyway;

    @Test
    void allMigrationsShouldBeApplied() {
        var info = flyway.info();
        assertThat(info.applied()).isNotEmpty();
        assertThat(info.pending()).isEmpty();
    }

    @Test
    void migrationCountShouldBe73() {
        var info = flyway.info();
        assertThat(info.applied().length).isEqualTo(73);
    }
}
