package com.refinedmods.refinedstorage2.api.core.filter;

import com.refinedmods.refinedstorage2.test.Rs2Test;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Rs2Test
class FilterTest {
    private Filter filter;

    @BeforeEach
    void setUp() {
        filter = new Filter();
    }

    @Test
    void Test_defaults() {
        // Assert
        assertThat(filter.getMode()).isEqualTo(FilterMode.BLOCK);
    }

    @Test
    void Test_empty_blocklist_allows_all() {
        // Arrange
        // Act
        boolean allowed = filter.isAllowed("Dirt");

        // Assert
        assertThat(allowed).isTrue();
    }

    @Test
    void Test_empty_allowlist_allows_none() {
        // Arrange
        filter.setMode(FilterMode.ALLOW);

        // Act
        boolean allowed = filter.isAllowed("Dirt");

        // Assert
        assertThat(allowed).isFalse();
    }

    @Test
    void Test_exact_allowlist() {
        // Arrange
        filter.setMode(FilterMode.ALLOW);
        filter.setTemplates(Set.of("Dirt", "Stone"));

        // Act
        boolean allowsDirt = filter.isAllowed("Dirt");
        boolean allowsStone = filter.isAllowed("Stone");
        boolean allowsSponge = filter.isAllowed("Sponge");

        // Assert
        assertThat(allowsDirt).isTrue();
        assertThat(allowsStone).isTrue();
        assertThat(allowsSponge).isFalse();
    }

    @Test
    void Test_exact_blocklist() {
        // Arrange
        filter.setTemplates(Set.of("Dirt", "Stone"));

        // Act
        boolean allowsDirt = filter.isAllowed("Dirt");
        boolean allowsStone = filter.isAllowed("Stone");
        boolean allowsSponge = filter.isAllowed("Sponge");

        // Assert
        assertThat(allowsDirt).isFalse();
        assertThat(allowsStone).isFalse();
        assertThat(allowsSponge).isTrue();
    }

    @Test
    void Test_changing_templates() {
        // Arrange
        filter.setTemplates(Set.of("Stone"));

        boolean allowsDirt = filter.isAllowed("Dirt");
        boolean allowsStone = filter.isAllowed("Stone");
        boolean allowsSponge = filter.isAllowed("Sponge");

        filter.setTemplates(Set.of("Dirt", "Sponge"));

        boolean allowsDirtAfter = filter.isAllowed("Dirt");
        boolean allowsStoneAfter = filter.isAllowed("Stone");
        boolean allowsSpongeAfter = filter.isAllowed("Sponge");

        // Assert
        assertThat(allowsDirt).isTrue();
        assertThat(allowsStone).isFalse();
        assertThat(allowsSponge).isTrue();

        assertThat(allowsDirtAfter).isFalse();
        assertThat(allowsStoneAfter).isTrue();
        assertThat(allowsSpongeAfter).isFalse();
    }
}
