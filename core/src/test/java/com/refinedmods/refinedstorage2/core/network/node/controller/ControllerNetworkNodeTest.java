package com.refinedmods.refinedstorage2.core.network.node.controller;

import com.refinedmods.refinedstorage2.core.Rs2Test;
import com.refinedmods.refinedstorage2.core.adapter.FakeRs2World;
import com.refinedmods.refinedstorage2.core.network.node.RedstoneMode;
import com.refinedmods.refinedstorage2.core.util.Action;
import com.refinedmods.refinedstorage2.core.util.Position;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

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
    @MethodSource("getStoredAndExpectedState")
    void Test_calculating_states(long stored, ControllerEnergyState expectedState) {
        // Arrange
        ControllerNetworkNode controller = new ControllerNetworkNode(Position.ORIGIN, 0, 100, ControllerType.NORMAL);
        controller.setWorld(new FakeRs2World());

        controller.receive(stored, Action.EXECUTE);

        // Act
        ControllerEnergyState state = controller.getState();

        // Assert
        assertThat(state).isEqualTo(expectedState);
    }

    @Test
    void Test_calculating_state_when_inactive() {
        // Arrange
        ControllerNetworkNode controller = new ControllerNetworkNode(Position.ORIGIN, 0, 100, ControllerType.NORMAL);
        controller.setRedstoneMode(RedstoneMode.HIGH);
        controller.setWorld(new FakeRs2World());

        // Act
        ControllerEnergyState state = controller.getState();

        // Assert
        assertThat(state).isEqualTo(ControllerEnergyState.OFF);
    }

    @ParameterizedTest
    @EnumSource(ControllerType.class)
    void Test_receiving_energy(ControllerType type) {
        // Arrange
        ControllerNetworkNode controller = new ControllerNetworkNode(Position.ORIGIN, 0, 100, type);
        controller.setWorld(new FakeRs2World());

        // Act
        long remainder = controller.receive(10, Action.EXECUTE);

        // Assert
        if (type == ControllerType.NORMAL) {
            assertThat(remainder).isZero();
            assertThat(controller.getCapacity()).isEqualTo(100);
            assertThat(controller.getStored()).isEqualTo(10);
        } else {
            assertThat(remainder).isEqualTo(10);
            assertThat(controller.getCapacity()).isEqualTo(Long.MAX_VALUE);
            assertThat(controller.getStored()).isEqualTo(Long.MAX_VALUE);
        }
    }

    @Test
    void Test_receiving_energy_when_inactive() {
        // Arrange
        ControllerNetworkNode controller = new ControllerNetworkNode(Position.ORIGIN, 0, 100, ControllerType.NORMAL);
        controller.setWorld(new FakeRs2World());

        controller.receive(5, Action.EXECUTE);
        controller.setRedstoneMode(RedstoneMode.HIGH);

        // Act
        long remainder = controller.receive(10, Action.EXECUTE);

        // Assert
        assertThat(remainder).isZero();
        assertThat(controller.getStored()).isZero();
        assertThat(controller.getCapacity()).isZero();
        assertThat(controller.getActualStored()).isEqualTo(15);
        assertThat(controller.getActualCapacity()).isEqualTo(100);
    }

    @ParameterizedTest
    @EnumSource(ControllerType.class)
    void Test_extracting_energy(ControllerType type) {
        // Arrange
        ControllerNetworkNode controller = new ControllerNetworkNode(Position.ORIGIN, 0, 100, type);
        controller.setWorld(new FakeRs2World());

        // Act
        controller.receive(10, Action.EXECUTE);
        long extracted = controller.extract(20, Action.EXECUTE);

        // Assert
        if (type == ControllerType.NORMAL) {
            assertThat(extracted).isEqualTo(10);
            assertThat(controller.getCapacity()).isEqualTo(100);
            assertThat(controller.getStored()).isZero();
        } else {
            assertThat(extracted).isEqualTo(20);
            assertThat(controller.getCapacity()).isEqualTo(Long.MAX_VALUE);
            assertThat(controller.getStored()).isEqualTo(Long.MAX_VALUE);
        }
    }

    @Test
    void Test_extracting_energy_when_inactive() {
        // Arrange
        ControllerNetworkNode controller = new ControllerNetworkNode(Position.ORIGIN, 0, 100, ControllerType.NORMAL);
        controller.setWorld(new FakeRs2World());

        controller.receive(20, Action.EXECUTE);
        controller.setRedstoneMode(RedstoneMode.HIGH);

        // Act
        long extracted = controller.extract(10, Action.EXECUTE);

        // Assert
        assertThat(extracted).isZero();
        assertThat(controller.getStored()).isZero();
        assertThat(controller.getCapacity()).isZero();
        assertThat(controller.getActualStored()).isEqualTo(20);
        assertThat(controller.getActualCapacity()).isEqualTo(100);
    }
}
