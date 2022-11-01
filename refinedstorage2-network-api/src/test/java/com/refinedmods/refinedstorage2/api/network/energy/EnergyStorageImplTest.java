package com.refinedmods.refinedstorage2.api.network.energy;

import com.refinedmods.refinedstorage2.api.core.Action;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EnergyStorageImplTest {
    @Test
    void testInvalidCapacity() {
        // Act
        final Executable action = () -> new EnergyStorageImpl(-1);

        // Assert
        assertThrows(Exception.class, action);
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldNotReceiveEnergyOnZeroCapacityStorage(final Action action) {
        // Arrange
        final EnergyStorage energyStorage = new EnergyStorageImpl(0);

        // Act
        final long inserted = energyStorage.receive(1, action);

        // Assert
        assertThat(inserted).isZero();
        assertThat(energyStorage.getStored()).isZero();
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldReceiveEnergy(final Action action) {
        // Arrange
        final EnergyStorage energyStorage = new EnergyStorageImpl(100);

        // Act
        final long inserted = energyStorage.receive(50, action);

        // Assert
        assertThat(inserted).isEqualTo(50);

        if (action == Action.EXECUTE) {
            assertThat(energyStorage.getStored()).isEqualTo(50);
        } else {
            assertThat(energyStorage.getStored()).isZero();
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldReceiveEnergyAndReachCapacity(final Action action) {
        // Arrange
        final EnergyStorage energyStorage = new EnergyStorageImpl(100);

        // Act
        final long inserted = energyStorage.receive(100, action);

        // Assert
        assertThat(inserted).isEqualTo(100);

        if (action == Action.EXECUTE) {
            assertThat(energyStorage.getStored()).isEqualTo(100);
        } else {
            assertThat(energyStorage.getStored()).isZero();
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldReceiveEnergyAndExceedCapacity(final Action action) {
        // Arrange
        final EnergyStorage energyStorage = new EnergyStorageImpl(100);

        // Act
        final long inserted = energyStorage.receive(101, action);

        // Assert
        assertThat(inserted).isEqualTo(100);

        if (action == Action.EXECUTE) {
            assertThat(energyStorage.getStored()).isEqualTo(100);
        } else {
            assertThat(energyStorage.getStored()).isZero();
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldNotReceiveEnergyWhenFull(final Action action) {
        // Arrange
        final EnergyStorage energyStorage = new EnergyStorageImpl(100);
        energyStorage.receive(100, Action.EXECUTE);

        // Act
        final long inserted = energyStorage.receive(100, action);

        // Assert
        assertThat(inserted).isZero();
        assertThat(energyStorage.getStored()).isEqualTo(100);
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldExtractEnergyPartly(final Action action) {
        // Arrange
        final EnergyStorage energyStorage = new EnergyStorageImpl(100);
        energyStorage.receive(100, Action.EXECUTE);

        // Act
        final long extracted = energyStorage.extract(99, action);

        // Assert
        assertThat(extracted).isEqualTo(99);

        if (action == Action.EXECUTE) {
            assertThat(energyStorage.getStored()).isEqualTo(1);
        } else {
            assertThat(energyStorage.getStored()).isEqualTo(100);
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldExtractEnergyCompletely(final Action action) {
        // Arrange
        final EnergyStorage energyStorage = new EnergyStorageImpl(100);
        energyStorage.receive(50, Action.EXECUTE);

        // Act
        final long extracted = energyStorage.extract(51, action);

        // Assert
        assertThat(extracted).isEqualTo(50);

        if (action == Action.EXECUTE) {
            assertThat(energyStorage.getStored()).isZero();
        } else {
            assertThat(energyStorage.getStored()).isEqualTo(50);
        }
    }
}
