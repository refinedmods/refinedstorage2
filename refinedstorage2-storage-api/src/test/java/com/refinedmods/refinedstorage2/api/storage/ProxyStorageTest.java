package com.refinedmods.refinedstorage2.api.storage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProxyStorageTest {
    private SourceCapturingStorage<String> backed;
    private AbstractProxyStorage<String> sut;
    private final Source customSource = () -> "Custom source";

    @BeforeEach
    void setUp() {
        backed = new SourceCapturingStorage<>(new InMemoryStorageImpl<>());
        sut = new AbstractProxyStorage<>(backed) {
        };
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void testInvalidParent() {
        // Act & assert
        assertThrows(NullPointerException.class, () -> new AbstractProxyStorage<String>(null) {
        });
    }

    @Test
    void shouldRetrieveAll() {
        // Arrange
        sut.insert("A", 10, Action.EXECUTE, EmptySource.INSTANCE);

        // Act & assert
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("A", 10)
        );
    }

    @Test
    void shouldRetrieveStoredAmount() {
        // Arrange
        sut.insert("A", 10, Action.EXECUTE, EmptySource.INSTANCE);

        // Act & assert
        assertThat(sut.getStored()).isEqualTo(10);
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldInsert(final Action action) {
        // Act
        sut.insert("A", 10, action, customSource);

        // Assert
        if (action == Action.EXECUTE) {
            assertThat(backed.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount<>("A", 10)
            );
        } else {
            assertThat(backed.getAll()).isEmpty();
        }
        assertThat(backed.getSourcesUsed()).containsExactly(customSource);
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldExtract(final Action action) {
        // Arrange
        backed.insert("A", 10, Action.EXECUTE, customSource);

        // Act
        final long extracted = sut.extract("A", 3, action, customSource);

        // Assert
        assertThat(extracted).isEqualTo(3);
        if (action == Action.EXECUTE) {
            assertThat(backed.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 7)
            );
        } else {
            assertThat(backed.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>("A", 10)
            );
        }
        assertThat(backed.getSourcesUsed()).containsExactly(customSource, customSource);
    }
}
