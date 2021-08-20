package com.refinedmods.refinedstorage2.core.network.energy;

import com.refinedmods.refinedstorage2.core.util.Action;
import com.refinedmods.refinedstorage2.test.Rs2Test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Rs2Test
class EnergyStorageImplTest {
    @Test
    void Test_invalid_capacity() {
        // Act
        Executable action = () -> new EnergyStorageImpl(-1);

        // Assert
        assertThrows(Exception.class, action);
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_receiving_energy_on_zero_capacity_storage(Action action) {
        // Arrange
        EnergyStorage energyStorage = new EnergyStorageImpl(0);

        // Act
        long remainder = energyStorage.receive(1, action);

        // Assert
        assertThat(remainder).isEqualTo(1);
        assertThat(energyStorage.getStored()).isZero();
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_receiving_energy(Action action) {
        // Arrange
        EnergyStorage energyStorage = new EnergyStorageImpl(100);

        // Act
        long remainder = energyStorage.receive(50, action);

        // Assert
        assertThat(remainder).isZero();

        if (action == Action.EXECUTE) {
            assertThat(energyStorage.getStored()).isEqualTo(50);
        } else {
            assertThat(energyStorage.getStored()).isZero();
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_receiving_energy_and_reaching_capacity(Action action) {
        // Arrange
        EnergyStorage energyStorage = new EnergyStorageImpl(100);

        // Act
        long remainder = energyStorage.receive(100, action);

        // Assert
        assertThat(remainder).isZero();

        if (action == Action.EXECUTE) {
            assertThat(energyStorage.getStored()).isEqualTo(100);
        } else {
            assertThat(energyStorage.getStored()).isZero();
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_receiving_energy_and_exceeding_capacity(Action action) {
        // Arrange
        EnergyStorage energyStorage = new EnergyStorageImpl(100);

        // Act
        long remainder = energyStorage.receive(101, action);

        // Assert
        assertThat(remainder).isEqualTo(1);

        if (action == Action.EXECUTE) {
            assertThat(energyStorage.getStored()).isEqualTo(100);
        } else {
            assertThat(energyStorage.getStored()).isZero();
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_extracting_energy_partly(Action action) {
        // Arrange
        EnergyStorage energyStorage = new EnergyStorageImpl(100);
        energyStorage.receive(100, Action.EXECUTE);

        // Act
        long extracted = energyStorage.extract(99, action);

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
    void Test_extracting_energy_completely(Action action) {
        // Arrange
        EnergyStorage energyStorage = new EnergyStorageImpl(100);
        energyStorage.receive(50, Action.EXECUTE);

        // Act
        long extracted = energyStorage.extract(51, action);

        // Assert
        assertThat(extracted).isEqualTo(50);

        if (action == Action.EXECUTE) {
            assertThat(energyStorage.getStored()).isZero();
        } else {
            assertThat(energyStorage.getStored()).isEqualTo(50);
        }
    }
}
