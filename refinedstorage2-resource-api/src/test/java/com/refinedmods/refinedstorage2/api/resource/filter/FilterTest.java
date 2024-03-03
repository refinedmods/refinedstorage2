package com.refinedmods.refinedstorage2.api.resource.filter;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage2.api.resource.TestResource.A;
import static com.refinedmods.refinedstorage2.api.resource.TestResource.B;
import static com.refinedmods.refinedstorage2.api.resource.TestResource.C;
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
        final boolean allowed = sut.isAllowed(A);

        // Assert
        assertThat(allowed).isTrue();
    }

    @Test
    void testEmptyAllowlistAllowsNone() {
        // Arrange
        sut.setMode(FilterMode.ALLOW);

        // Act
        final boolean allowed = sut.isAllowed(A);

        // Assert
        assertThat(allowed).isFalse();
    }

    @Test
    void testAllowlist() {
        // Arrange
        sut.setMode(FilterMode.ALLOW);
        sut.setTemplates(Set.of(A, B));

        // Act
        final boolean allowsDirt = sut.isAllowed(A);
        final boolean allowsStone = sut.isAllowed(B);
        final boolean allowsSponge = sut.isAllowed(C);

        // Assert
        assertThat(allowsDirt).isTrue();
        assertThat(allowsStone).isTrue();
        assertThat(allowsSponge).isFalse();
    }

    @Test
    void testBlocklist() {
        // Arrange
        sut.setTemplates(Set.of(A, B));

        // Act
        final boolean allowsDirt = sut.isAllowed(A);
        final boolean allowsStone = sut.isAllowed(B);
        final boolean allowsSponge = sut.isAllowed(C);

        // Assert
        assertThat(allowsDirt).isFalse();
        assertThat(allowsStone).isFalse();
        assertThat(allowsSponge).isTrue();
    }

    @Test
    void shouldBeAbleToModifyTemplates() {
        // Arrange
        sut.setTemplates(Set.of(B));

        final boolean allowsDirt = sut.isAllowed(A);
        final boolean allowsStone = sut.isAllowed(B);
        final boolean allowsSponge = sut.isAllowed(C);

        // Act
        sut.setTemplates(Set.of(A, C));

        final boolean allowsDirtAfter = sut.isAllowed(A);
        final boolean allowsStoneAfter = sut.isAllowed(B);
        final boolean allowsSpongeAfter = sut.isAllowed(C);

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
        sut.setNormalizer(resource -> {
            if (resource == A) {
                return B;
            }
            return resource;
        });
        sut.setMode(FilterMode.ALLOW);
        sut.setTemplates(Set.of(A));

        // Act & assert
        assertThat(sut.isAllowed(A)).isTrue();
        assertThat(sut.isAllowed(B)).isTrue();
        assertThat(sut.isAllowed(C)).isFalse();
    }

    @Test
    void testBlocklistNormalizer() {
        // Arrange
        sut.setNormalizer(resource -> {
            if (resource == A) {
                return B;
            }
            return resource;
        });
        sut.setMode(FilterMode.BLOCK);
        sut.setTemplates(Set.of(A));

        // Act & assert
        assertThat(sut.isAllowed(A)).isFalse();
        assertThat(sut.isAllowed(B)).isFalse();
        assertThat(sut.isAllowed(C)).isTrue();
    }
}
