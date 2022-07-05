package com.refinedmods.refinedstorage2.api.network.node.controller;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorageImpl;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class ControllerNetworkNodeTest {
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldAlwaysBeActive(final boolean explicitlyActive) {
        // Arrange
        final ControllerNetworkNode sut = new ControllerNetworkNode();
        sut.setEnergyStorage(new EnergyStorageImpl(100));
        if (explicitlyActive) {
            sut.setActivenessProvider(() -> true);
        }

        // Assert
        assertThat(sut.isActive()).isTrue();
        assertThat(sut.getEnergyUsage()).isZero();
    }

    @Test
    void shouldAlwaysBeActiveUnlessToldNotTo() {
        // Arrange
        final ControllerNetworkNode sut = new ControllerNetworkNode();
        sut.setEnergyStorage(new EnergyStorageImpl(100));
        sut.setActivenessProvider(() -> false);

        // Assert
        assertThat(sut.isActive()).isFalse();
        assertThat(sut.getEnergyUsage()).isZero();
    }

    @Test
    void testStoredAndCapacityWhenInactive() {
        // Arrange
        final ControllerNetworkNode sut = new ControllerNetworkNode();
        sut.setEnergyStorage(new EnergyStorageImpl(100));
        sut.receive(10, Action.EXECUTE);

        sut.setActivenessProvider(() -> false);

        // Act
        final long stored = sut.getStored();
        final long actualStored = sut.getActualStored();
        final long capacity = sut.getCapacity();
        final long actualCapacity = sut.getActualCapacity();

        // Assert
        assertThat(stored).isZero();
        assertThat(actualStored).isEqualTo(10);
        assertThat(capacity).isZero();
        assertThat(actualCapacity).isEqualTo(100);
    }

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
    @MethodSource("getStoredAndExpectedState")
    void testCalculatingStates(final long stored, final ControllerEnergyState expectedState) {
        // Arrange
        final ControllerNetworkNode sut = new ControllerNetworkNode();
        sut.setEnergyStorage(new EnergyStorageImpl(100));
        sut.receive(stored, Action.EXECUTE);

        // Act
        final ControllerEnergyState state = sut.getState();

        // Assert
        assertThat(state).isEqualTo(expectedState);
    }

    @Test
    void testEnergyStateShouldBeOffWhenInactive() {
        // Arrange
        final ControllerNetworkNode sut = new ControllerNetworkNode();
        sut.setEnergyStorage(new EnergyStorageImpl(100));
        sut.receive(50, Action.EXECUTE);
        sut.setActivenessProvider(() -> false);

        // Act
        final ControllerEnergyState state = sut.getState();

        // Assert
        assertThat(state).isEqualTo(ControllerEnergyState.OFF);
    }

    @Test
    void shouldReceiveEnergy() {
        // Arrange
        final ControllerNetworkNode sut = new ControllerNetworkNode();
        sut.setEnergyStorage(new EnergyStorageImpl(100));

        // Act
        final long inserted = sut.receive(10, Action.EXECUTE);

        // Assert
        assertThat(inserted).isEqualTo(10);
        assertThat(sut.getCapacity()).isEqualTo(100);
        assertThat(sut.getActualCapacity()).isEqualTo(100);
        assertThat(sut.getStored()).isEqualTo(10);
        assertThat(sut.getActualStored()).isEqualTo(10);
    }

    @Test
    void shouldExtractEnergy() {
        // Arrange
        final ControllerNetworkNode sut = new ControllerNetworkNode();
        sut.setEnergyStorage(new EnergyStorageImpl(100));

        // Act
        sut.receive(10, Action.EXECUTE);
        final long extracted = sut.extract(20, Action.EXECUTE);

        // Assert
        assertThat(extracted).isEqualTo(10);
        assertThat(sut.getCapacity()).isEqualTo(100);
        assertThat(sut.getActualCapacity()).isEqualTo(100);
        assertThat(sut.getStored()).isZero();
        assertThat(sut.getActualStored()).isZero();
    }
}
