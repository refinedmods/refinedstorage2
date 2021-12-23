package com.refinedmods.refinedstorage2.api.core.filter;

import com.refinedmods.refinedstorage2.test.Rs2Test;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Rs2Test
class FilterTest {
    private Filter sut;

    @BeforeEach
    void setUp() {
        sut = new Filter();
    }

    @Test
    void Test_defaults() {
        // Assert
        assertThat(sut.getMode()).isEqualTo(FilterMode.BLOCK);
    }

    @Test
    void Test_empty_blocklist_allows_all() {
        // Arrange
        // Act
        boolean allowed = sut.isAllowed("Dirt");

        // Assert
        assertThat(allowed).isTrue();
    }

    @Test
    void Test_empty_allowlist_allows_none() {
        // Arrange
        sut.setMode(FilterMode.ALLOW);

        // Act
        boolean allowed = sut.isAllowed("Dirt");

        // Assert
        assertThat(allowed).isFalse();
    }

    @Test
    void Test_exact_allowlist() {
        // Arrange
        sut.setMode(FilterMode.ALLOW);
        sut.setTemplates(Set.of("Dirt", "Stone"));

        // Act
        boolean allowsDirt = sut.isAllowed("Dirt");
        boolean allowsStone = sut.isAllowed("Stone");
        boolean allowsSponge = sut.isAllowed("Sponge");

        // Assert
        assertThat(allowsDirt).isTrue();
        assertThat(allowsStone).isTrue();
        assertThat(allowsSponge).isFalse();
    }

    @Test
    void Test_exact_blocklist() {
        // Arrange
        sut.setTemplates(Set.of("Dirt", "Stone"));

        // Act
        boolean allowsDirt = sut.isAllowed("Dirt");
        boolean allowsStone = sut.isAllowed("Stone");
        boolean allowsSponge = sut.isAllowed("Sponge");

        // Assert
        assertThat(allowsDirt).isFalse();
        assertThat(allowsStone).isFalse();
        assertThat(allowsSponge).isTrue();
    }

    @Test
    void Test_changing_templates() {
        // Arrange
        sut.setTemplates(Set.of("Stone"));

        boolean allowsDirt = sut.isAllowed("Dirt");
        boolean allowsStone = sut.isAllowed("Stone");
        boolean allowsSponge = sut.isAllowed("Sponge");

        sut.setTemplates(Set.of("Dirt", "Sponge"));

        boolean allowsDirtAfter = sut.isAllowed("Dirt");
        boolean allowsStoneAfter = sut.isAllowed("Stone");
        boolean allowsSpongeAfter = sut.isAllowed("Sponge");

        // Assert
        assertThat(allowsDirt).isTrue();
        assertThat(allowsStone).isFalse();
        assertThat(allowsSponge).isTrue();

        assertThat(allowsDirtAfter).isFalse();
        assertThat(allowsStoneAfter).isTrue();
        assertThat(allowsSpongeAfter).isFalse();
    }

    @Test
    void Test_normalizing_with_allowlist() {
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
    void Test_normalizing_with_blocklist() {
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
