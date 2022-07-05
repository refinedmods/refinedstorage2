package com.refinedmods.refinedstorage2.api.network.energy;

import com.refinedmods.refinedstorage2.api.core.Action;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class CompositeEnergyStorageTest {
    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldNotReceiveEnergyWhenNoSourcesArePresent(final Action action) {
        // Arrange
        final CompositeEnergyStorage sut = new CompositeEnergyStorage();

        // Act
        final long inserted = sut.receive(3, action);

        // Assert
        assertThat(inserted).isZero();
        assertThat(sut.getStored()).isZero();
        assertThat(sut.getCapacity()).isZero();
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldReceiveSingleSourcePartially(final Action action) {
        // Arrange
        final EnergyStorage a = new EnergyStorageImpl(10);
        final EnergyStorage b = new EnergyStorageImpl(5);

        final CompositeEnergyStorage sut = new CompositeEnergyStorage();
        sut.addSource(a);
        sut.addSource(b);

        // Act
        final long inserted = sut.receive(3, action);

        // Assert
        assertThat(inserted).isEqualTo(3);

        if (action == Action.EXECUTE) {
            assertThat(a.getStored()).isEqualTo(3);
            assertThat(sut.getStored()).isEqualTo(3);
        } else {
            assertThat(a.getStored()).isZero();
            assertThat(sut.getStored()).isZero();
        }

        assertThat(b.getStored()).isZero();
        assertThat(sut.getCapacity()).isEqualTo(15);
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldReceiveSingleSourceCompletely(final Action action) {
        // Arrange
        final EnergyStorage a = new EnergyStorageImpl(10);
        final EnergyStorage b = new EnergyStorageImpl(5);

        final CompositeEnergyStorage sut = new CompositeEnergyStorage();
        sut.addSource(a);
        sut.addSource(b);

        // Act
        final long inserted = sut.receive(10, action);

        // Assert
        if (action == Action.EXECUTE) {
            assertThat(a.getStored()).isEqualTo(10);
            assertThat(sut.getStored()).isEqualTo(10);
        } else {
            assertThat(a.getStored()).isZero();
            assertThat(sut.getStored()).isZero();
        }

        assertThat(inserted).isEqualTo(10);
        assertThat(b.getStored()).isZero();
        assertThat(sut.getCapacity()).isEqualTo(15);
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldReceiveIntoMultipleSourcesPartially(final Action action) {
        // Arrange
        final EnergyStorage a = new EnergyStorageImpl(10);
        final EnergyStorage b = new EnergyStorageImpl(5);

        final CompositeEnergyStorage sut = new CompositeEnergyStorage();
        sut.addSource(a);
        sut.addSource(b);

        // Act
        final long inserted = sut.receive(13, action);

        // Assert
        assertThat(inserted).isEqualTo(13);

        if (action == Action.EXECUTE) {
            assertThat(a.getStored()).isEqualTo(10);
            assertThat(b.getStored()).isEqualTo(3);
            assertThat(sut.getStored()).isEqualTo(13);
        } else {
            assertThat(a.getStored()).isZero();
            assertThat(b.getStored()).isZero();
            assertThat(sut.getStored()).isZero();
        }

        assertThat(sut.getCapacity()).isEqualTo(15);
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldReceiveIntoMultipleSourcesCompletely(final Action action) {
        // Arrange
        final EnergyStorage a = new EnergyStorageImpl(10);
        final EnergyStorage b = new EnergyStorageImpl(5);

        final CompositeEnergyStorage sut = new CompositeEnergyStorage();
        sut.addSource(a);
        sut.addSource(b);

        // Act
        final long inserted = sut.receive(15, action);

        // Assert
        assertThat(inserted).isEqualTo(15);

        if (action == Action.EXECUTE) {
            assertThat(a.getStored()).isEqualTo(10);
            assertThat(b.getStored()).isEqualTo(5);
            assertThat(sut.getStored()).isEqualTo(15);
        } else {
            assertThat(a.getStored()).isZero();
            assertThat(b.getStored()).isZero();
            assertThat(sut.getStored()).isZero();
        }

        assertThat(sut.getCapacity()).isEqualTo(15);
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldReceiveIntoMultipleSourcesCompletelyWithRemainder(final Action action) {
        // Arrange
        final EnergyStorage a = new EnergyStorageImpl(10);
        final EnergyStorage b = new EnergyStorageImpl(5);

        final CompositeEnergyStorage sut = new CompositeEnergyStorage();
        sut.addSource(a);
        sut.addSource(b);

        // Act
        final long inserted = sut.receive(16, action);

        // Assert
        assertThat(inserted).isEqualTo(15);

        if (action == Action.EXECUTE) {
            assertThat(a.getStored()).isEqualTo(10);
            assertThat(b.getStored()).isEqualTo(5);
            assertThat(sut.getStored()).isEqualTo(15);
        } else {
            assertThat(a.getStored()).isZero();
            assertThat(b.getStored()).isZero();
            assertThat(sut.getStored()).isZero();
        }

        assertThat(sut.getCapacity()).isEqualTo(15);
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldNotExtractAnythingWhenNoSourcesAreAvailable(final Action action) {
        // Arrange
        final CompositeEnergyStorage sut = new CompositeEnergyStorage();

        // Act
        final long extracted = sut.extract(3, action);

        // Assert
        assertThat(extracted).isZero();
        assertThat(sut.getStored()).isZero();
        assertThat(sut.getCapacity()).isZero();
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldExtractFromSingleSourcePartly(final Action action) {
        // Arrange
        final EnergyStorage a = new EnergyStorageImpl(10);
        final EnergyStorage b = new EnergyStorageImpl(5);

        a.receive(10, Action.EXECUTE);
        b.receive(3, Action.EXECUTE);

        final CompositeEnergyStorage sut = new CompositeEnergyStorage();
        sut.addSource(a);
        sut.addSource(b);

        // Act
        final long extracted = sut.extract(3, action);

        // Assert
        assertThat(extracted).isEqualTo(3);

        if (action == Action.EXECUTE) {
            assertThat(a.getStored()).isEqualTo(7);
            assertThat(b.getStored()).isEqualTo(3);
        } else {
            assertThat(a.getStored()).isEqualTo(10);
            assertThat(b.getStored()).isEqualTo(3);
        }

        assertThat(sut.getCapacity()).isEqualTo(15);
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldExtractFromSingleSourceCompletely(final Action action) {
        // Arrange
        final EnergyStorage a = new EnergyStorageImpl(10);
        final EnergyStorage b = new EnergyStorageImpl(5);

        a.receive(10, Action.EXECUTE);
        b.receive(3, Action.EXECUTE);

        final CompositeEnergyStorage sut = new CompositeEnergyStorage();
        sut.addSource(a);
        sut.addSource(b);

        // Act
        final long extracted = sut.extract(10, action);

        // Assert
        assertThat(extracted).isEqualTo(10);

        if (action == Action.EXECUTE) {
            assertThat(a.getStored()).isZero();
            assertThat(b.getStored()).isEqualTo(3);
        } else {
            assertThat(a.getStored()).isEqualTo(10);
            assertThat(b.getStored()).isEqualTo(3);
        }

        assertThat(sut.getCapacity()).isEqualTo(15);
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldExtractFromMultipleSourcesPartly(final Action action) {
        // Arrange
        final EnergyStorage a = new EnergyStorageImpl(10);
        final EnergyStorage b = new EnergyStorageImpl(5);

        a.receive(10, Action.EXECUTE);
        b.receive(3, Action.EXECUTE);

        final CompositeEnergyStorage sut = new CompositeEnergyStorage();
        sut.addSource(a);
        sut.addSource(b);

        // Act
        final long extracted = sut.extract(11, action);

        // Assert
        assertThat(extracted).isEqualTo(11);

        if (action == Action.EXECUTE) {
            assertThat(a.getStored()).isZero();
            assertThat(b.getStored()).isEqualTo(2);
        } else {
            assertThat(a.getStored()).isEqualTo(10);
            assertThat(b.getStored()).isEqualTo(3);
        }

        assertThat(sut.getCapacity()).isEqualTo(15);
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldExtractFromMultipleSourcesCompletely(final Action action) {
        // Arrange
        final EnergyStorage a = new EnergyStorageImpl(10);
        final EnergyStorage b = new EnergyStorageImpl(5);

        a.receive(10, Action.EXECUTE);
        b.receive(3, Action.EXECUTE);

        final CompositeEnergyStorage sut = new CompositeEnergyStorage();
        sut.addSource(a);
        sut.addSource(b);

        // Act
        final long extracted = sut.extract(13, action);

        // Assert
        assertThat(extracted).isEqualTo(13);

        if (action == Action.EXECUTE) {
            assertThat(a.getStored()).isZero();
            assertThat(b.getStored()).isZero();
        } else {
            assertThat(a.getStored()).isEqualTo(10);
            assertThat(b.getStored()).isEqualTo(3);
        }

        assertThat(sut.getCapacity()).isEqualTo(15);
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldExtractFromMultipleSourcesCompletelyMoreThanIsAvailable(final Action action) {
        // Arrange
        final EnergyStorage a = new EnergyStorageImpl(10);
        final EnergyStorage b = new EnergyStorageImpl(5);

        a.receive(10, Action.EXECUTE);
        b.receive(3, Action.EXECUTE);

        final CompositeEnergyStorage sut = new CompositeEnergyStorage();
        sut.addSource(a);
        sut.addSource(b);

        // Act
        final long extracted = sut.extract(14, action);

        // Assert
        assertThat(extracted).isEqualTo(13);

        if (action == Action.EXECUTE) {
            assertThat(a.getStored()).isZero();
            assertThat(b.getStored()).isZero();
        } else {
            assertThat(a.getStored()).isEqualTo(10);
            assertThat(b.getStored()).isEqualTo(3);
        }

        assertThat(sut.getCapacity()).isEqualTo(15);
    }

    @Test
    void shouldNotOverflowStoredAndCapacityCountOnInfiniteEnergyStoragesStoredInComposite() {
        // Arrange
        final EnergyStorage a = new InfiniteEnergyStorage();
        final EnergyStorage b = new InfiniteEnergyStorage();

        final CompositeEnergyStorage sut = new CompositeEnergyStorage();
        sut.addSource(a);
        sut.addSource(b);

        // Act
        final long stored = sut.getStored();
        final long capacity = sut.getCapacity();

        // Assert
        assertThat(stored).isEqualTo(Long.MAX_VALUE);
        assertThat(capacity).isEqualTo(Long.MAX_VALUE);
    }
}
