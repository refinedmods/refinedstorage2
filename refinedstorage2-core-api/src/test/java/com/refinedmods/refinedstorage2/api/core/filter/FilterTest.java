package com.refinedmods.refinedstorage2.api.core.filter;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FilterTest {
    private Filter sut;

    @BeforeEach
    void setUp() {
        sut = new Filter();
    }

    @Test
    void testDefaults() {
        // Assert
        assertThat(sut.getMode()).isEqualTo(FilterMode.BLOCK);
    }

    @Test
    void testEmptyBlocklistShouldAllowAll() {
        // Act
        final boolean allowed = sut.isAllowed("Dirt");

        // Assert
        assertThat(allowed).isTrue();
    }

    @Test
    void testEmptyAllowlistAllowsNone() {
        // Arrange
        sut.setMode(FilterMode.ALLOW);

        // Act
        final boolean allowed = sut.isAllowed("Dirt");

        // Assert
        assertThat(allowed).isFalse();
    }

    @Test
    void testAllowlist() {
        // Arrange
        sut.setMode(FilterMode.ALLOW);
        sut.setTemplates(Set.of("Dirt", "Stone"));

        // Act
        final boolean allowsDirt = sut.isAllowed("Dirt");
        final boolean allowsStone = sut.isAllowed("Stone");
        final boolean allowsSponge = sut.isAllowed("Sponge");

        // Assert
        assertThat(allowsDirt).isTrue();
        assertThat(allowsStone).isTrue();
        assertThat(allowsSponge).isFalse();
    }

    @Test
    void testBlocklist() {
        // Arrange
        sut.setTemplates(Set.of("Dirt", "Stone"));

        // Act
        final boolean allowsDirt = sut.isAllowed("Dirt");
        final boolean allowsStone = sut.isAllowed("Stone");
        final boolean allowsSponge = sut.isAllowed("Sponge");

        // Assert
        assertThat(allowsDirt).isFalse();
        assertThat(allowsStone).isFalse();
        assertThat(allowsSponge).isTrue();
    }

    @Test
    void shouldBeAbleToModifyTemplates() {
        // Arrange
        sut.setTemplates(Set.of("Stone"));

        final boolean allowsDirt = sut.isAllowed("Dirt");
        final boolean allowsStone = sut.isAllowed("Stone");
        final boolean allowsSponge = sut.isAllowed("Sponge");

        // Act
        sut.setTemplates(Set.of("Dirt", "Sponge"));

        final boolean allowsDirtAfter = sut.isAllowed("Dirt");
        final boolean allowsStoneAfter = sut.isAllowed("Stone");
        final boolean allowsSpongeAfter = sut.isAllowed("Sponge");

        // Assert
        assertThat(allowsDirt).isTrue();
        assertThat(allowsStone).isFalse();
        assertThat(allowsSponge).isTrue();

        assertThat(allowsDirtAfter).isFalse();
        assertThat(allowsStoneAfter).isTrue();
        assertThat(allowsSpongeAfter).isFalse();
    }

    @Test
    void testAllowlistNormalizer() {
        // Arrange
        sut.setNormalizer(n -> {
            if (n instanceof String str && !str.endsWith("!")) {
                return str + "!";
            }
            return n;
        });
        sut.setMode(FilterMode.ALLOW);
        sut.setTemplates(Set.of("A", "B"));

        // Act & assert
        assertThat(sut.isAllowed("A")).isTrue();
        assertThat(sut.isAllowed("A!")).isTrue();
        assertThat(sut.isAllowed("B")).isTrue();
        assertThat(sut.isAllowed("B!")).isTrue();
        assertThat(sut.isAllowed("C")).isFalse();
        assertThat(sut.isAllowed("C!")).isFalse();
    }

    @Test
    void testBlocklistNormalizer() {
        // Arrange
        sut.setNormalizer(n -> {
            if (n instanceof String str && !str.endsWith("!")) {
                return str + "!";
            }
            return n;
        });
        sut.setMode(FilterMode.BLOCK);
        sut.setTemplates(Set.of("A", "B"));

        // Act & assert
        assertThat(sut.isAllowed("A")).isFalse();
        assertThat(sut.isAllowed("A!")).isFalse();
        assertThat(sut.isAllowed("B")).isFalse();
        assertThat(sut.isAllowed("B!")).isFalse();
        assertThat(sut.isAllowed("C")).isTrue();
        assertThat(sut.isAllowed("C!")).isTrue();
    }
}
