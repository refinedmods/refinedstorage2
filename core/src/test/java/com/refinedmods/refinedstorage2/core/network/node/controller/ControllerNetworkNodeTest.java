package com.refinedmods.refinedstorage2.core.network.node.controller;

import com.refinedmods.refinedstorage2.core.Rs2Test;
import com.refinedmods.refinedstorage2.core.util.Action;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

@Rs2Test
class ControllerNetworkNodeTest {
    private static Stream<Arguments> states() {
        return Stream.of(
                Arguments.of(0, ControllerEnergyState.OFF),
                Arguments.of(1, ControllerEnergyState.NEARLY_OFF),
                Arguments.of(10, ControllerEnergyState.NEARLY_OFF),
                Arguments.of(11, ControllerEnergyState.NEARLY_ON),
                Arguments.of(30, ControllerEnergyState.NEARLY_ON),
                Arguments.of(31, ControllerEnergyState.ON),
                Arguments.of(50, ControllerEnergyState.ON),
                Arguments.of(100, ControllerEnergyState.ON)
        );
    }

    @ParameterizedTest
    @MethodSource("states")
    void Test_percentages(long stored, ControllerEnergyState expectedState) {
        // Arrange
        ControllerNetworkNode networkNode = new ControllerNetworkNode(null, null, null, 100, ControllerType.NORMAL);

        networkNode.receive(stored, Action.EXECUTE);

        // Act
        ControllerEnergyState state = networkNode.getState();

        // Assert
        assertThat(state).isEqualTo(expectedState);
    }
}
