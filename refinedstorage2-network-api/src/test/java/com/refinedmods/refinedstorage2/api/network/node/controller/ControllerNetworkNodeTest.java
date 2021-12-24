package com.refinedmods.refinedstorage2.api.network.node.controller;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorageImpl;
import com.refinedmods.refinedstorage2.test.Rs2Test;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@Rs2Test
class ControllerNetworkNodeTest {
    private static Stream<Arguments> getStoredAndExpectedState() {
        return Stream.of(
                Arguments.of(0, ControllerEnergyState.OFF),
                Arguments.of(1, ControllerEnergyState.NEARLY_OFF),
                Arguments.of(29, ControllerEnergyState.NEARLY_OFF),
                Arguments.of(30, ControllerEnergyState.NEARLY_ON),
                Arguments.of(39, ControllerEnergyState.NEARLY_ON),
                Arguments.of(40, ControllerEnergyState.ON),
                Arguments.of(50, ControllerEnergyState.ON),
                Arguments.of(100, ControllerEnergyState.ON)
        );
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void Test_always_active(boolean explicitlyActive) {
        // Arrange
        ControllerNetworkNode sut = new ControllerNetworkNode();
        sut.setEnergyStorage(new EnergyStorageImpl(100));
        if (explicitlyActive) {
            sut.setActivenessProvider(() -> true);
        }

        // Assert
        assertThat(sut.isActive()).isTrue();
        assertThat(sut.getEnergyUsage()).isZero();
    }

    @Test
    void Test_always_active_unless_told_not_to() {
        // Arrange
        ControllerNetworkNode sut = new ControllerNetworkNode();
        sut.setEnergyStorage(new EnergyStorageImpl(100));
        sut.setActivenessProvider(() -> false);

        // Assert
        assertThat(sut.isActive()).isFalse();
        assertThat(sut.getEnergyUsage()).isZero();
    }

    @Test
    void Test_stored_and_capacity_when_inactive() {
        // Arrange
        ControllerNetworkNode sut = new ControllerNetworkNode();
        sut.setEnergyStorage(new EnergyStorageImpl(100));
        sut.receive(10, Action.EXECUTE);

        sut.setActivenessProvider(() -> false);

        // Act
        long stored = sut.getStored();
        long actualStored = sut.getActualStored();
        long capacity = sut.getCapacity();
        long actualCapacity = sut.getActualCapacity();

        // Assert
        assertThat(stored).isZero();
        assertThat(actualStored).isEqualTo(10);
        assertThat(capacity).isZero();
        assertThat(actualCapacity).isEqualTo(100);
    }

    @ParameterizedTest
    @MethodSource("getStoredAndExpectedState")
    void Test_calculating_states(long stored, ControllerEnergyState expectedState) {
        // Arrange
        ControllerNetworkNode sut = new ControllerNetworkNode();
        sut.setEnergyStorage(new EnergyStorageImpl(100));
        sut.receive(stored, Action.EXECUTE);

        // Act
        ControllerEnergyState state = sut.getState();

        // Assert
        assertThat(state).isEqualTo(expectedState);
    }

    @Test
    void Test_receiving_energy() {
        // Arrange
        ControllerNetworkNode sut = new ControllerNetworkNode();
        sut.setEnergyStorage(new EnergyStorageImpl(100));

        // Act
        long remainder = sut.receive(10, Action.EXECUTE);

        // Assert
        assertThat(remainder).isZero();
        assertThat(sut.getCapacity()).isEqualTo(100);
        assertThat(sut.getActualCapacity()).isEqualTo(100);
        assertThat(sut.getStored()).isEqualTo(10);
        assertThat(sut.getActualStored()).isEqualTo(10);
    }

    @Test
    void Test_extracting_energy() {
        // Arrange
        ControllerNetworkNode sut = new ControllerNetworkNode();
        sut.setEnergyStorage(new EnergyStorageImpl(100));

        // Act
        sut.receive(10, Action.EXECUTE);
        long extracted = sut.extract(20, Action.EXECUTE);

        // Assert
        assertThat(extracted).isEqualTo(10);
        assertThat(sut.getCapacity()).isEqualTo(100);
        assertThat(sut.getActualCapacity()).isEqualTo(100);
        assertThat(sut.getStored()).isZero();
        assertThat(sut.getActualStored()).isZero();
    }
}
