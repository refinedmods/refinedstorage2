package com.refinedmods.refinedstorage2.api.network.node.controller;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.test.Rs2Test;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.verification.VerificationMode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
        ControllerNetworkNode controller = new ControllerNetworkNode(0, 100, ControllerType.NORMAL, mock(ControllerListener.class));

        controller.receive(stored, Action.EXECUTE);

        // Act
        ControllerEnergyState state = controller.getState();

        // Assert
        assertThat(state).isEqualTo(expectedState);
    }

    @Test
    void Test_calculating_state_when_inactive() {
        // Arrange
        ControllerNetworkNode controller = new ControllerNetworkNode(0, 100, ControllerType.NORMAL, mock(ControllerListener.class));
        controller.setActive(false);

        // Act
        ControllerEnergyState state = controller.getState();

        // Assert
        assertThat(state).isEqualTo(ControllerEnergyState.OFF);
    }

    @ParameterizedTest
    @EnumSource(ControllerType.class)
    void Test_receiving_energy(ControllerType type) {
        // Arrange
        ControllerNetworkNode controller = new ControllerNetworkNode(0, 100, type, mock(ControllerListener.class));

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
        ControllerNetworkNode controller = new ControllerNetworkNode(0, 100, ControllerType.NORMAL, mock(ControllerListener.class));

        controller.receive(5, Action.EXECUTE);
        controller.setActive(false);

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
        ControllerNetworkNode controller = new ControllerNetworkNode(0, 100, type, mock(ControllerListener.class));

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
        ControllerNetworkNode controller = new ControllerNetworkNode(0, 100, ControllerType.NORMAL, mock(ControllerListener.class));

        controller.receive(20, Action.EXECUTE);
        controller.setActive(false);

        // Act
        long extracted = controller.extract(10, Action.EXECUTE);

        // Assert
        assertThat(extracted).isZero();
        assertThat(controller.getStored()).isZero();
        assertThat(controller.getCapacity()).isZero();
        assertThat(controller.getActualStored()).isEqualTo(20);
        assertThat(controller.getActualCapacity()).isEqualTo(100);
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_should_call_listener_when_receiving_when_necessary(Action action) {
        // Arrange
        ControllerListener listener = mock(ControllerListener.class);

        ControllerNetworkNode controller = new ControllerNetworkNode(50, 100, ControllerType.NORMAL, listener);

        // Act
        controller.receive(75, action);

        // Assert
        VerificationMode times = action == Action.EXECUTE ? times(1) : never();

        verify(listener, times).onEnergyChanged();
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_should_not_call_listener_when_receiving_when_not_necessary(Action action) {
        // Arrange
        ControllerListener listener = mock(ControllerListener.class);

        ControllerNetworkNode controller = new ControllerNetworkNode(100, 100, ControllerType.NORMAL, listener);

        // Act
        controller.receive(10, action);

        // Assert
        verify(listener, never()).onEnergyChanged();
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_should_call_listener_when_extracting_when_necessary(Action action) {
        // Arrange
        ControllerListener listener = mock(ControllerListener.class);

        ControllerNetworkNode controller = new ControllerNetworkNode(50, 100, ControllerType.NORMAL, listener);

        // Act
        controller.extract(75, action);

        // Assert
        VerificationMode times = action == Action.EXECUTE ? times(1) : never();

        verify(listener, times).onEnergyChanged();
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_should_not_call_listener_when_extracting_when_not_necessary(Action action) {
        // Arrange
        ControllerListener listener = mock(ControllerListener.class);

        ControllerNetworkNode controller = new ControllerNetworkNode(0, 100, ControllerType.NORMAL, listener);

        // Act
        controller.extract(10, action);

        // Assert
        verify(listener, never()).onEnergyChanged();
    }
}
