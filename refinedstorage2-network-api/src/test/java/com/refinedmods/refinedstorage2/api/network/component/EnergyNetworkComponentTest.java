package com.refinedmods.refinedstorage2.api.network.component;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorageImpl;
import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage2.api.network.node.controller.ControllerNetworkNode;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EnergyNetworkComponentTest {
    @Test
    void testInitialState() {
        // Arrange
        final EnergyNetworkComponent sut = new EnergyNetworkComponent();

        // Assert
        assertThat(sut.getStored()).isZero();
        assertThat(sut.getCapacity()).isZero();
    }

    @Test
    void shouldUpdateEnergyStorageAfterAddingNode() {
        // Arrange
        final EnergyNetworkComponent sut = new EnergyNetworkComponent();

        final ControllerNetworkNode controller = new ControllerNetworkNode();
        controller.setActive(true);
        controller.setEnergyStorage(new EnergyStorageImpl(1000));
        controller.receive(100, Action.EXECUTE);
        final NetworkNodeContainer container = () -> controller;

        // Act
        final long extractedBefore = sut.extract(1);
        final long capacityBefore = sut.getCapacity();
        final long storedBefore = sut.getStored();

        sut.onContainerAdded(container);

        final long extractedAfter = sut.extract(1);
        final long capacityAfter = sut.getCapacity();
        final long storedAfter = sut.getStored();

        // Assert
        assertThat(extractedBefore).isZero();
        assertThat(capacityBefore).isZero();
        assertThat(storedBefore).isZero();

        assertThat(capacityAfter).isEqualTo(1000);
        assertThat(storedAfter).isEqualTo(99);
        assertThat(extractedAfter).isEqualTo(1);
    }

    @Test
    void shouldUpdateEnergyStorageAfterRemovingNode() {
        // Arrange
        final EnergyNetworkComponent sut = new EnergyNetworkComponent();

        final ControllerNetworkNode controller = new ControllerNetworkNode();
        controller.setEnergyStorage(new EnergyStorageImpl(1000));
        controller.receive(100, Action.EXECUTE);
        controller.setActive(true);
        final NetworkNodeContainer container = () -> controller;

        sut.onContainerAdded(container);

        final long capacityBefore = sut.getCapacity();
        final long storedBefore = sut.getStored();

        // Act
        sut.onContainerRemoved(container);

        final long capacityAfter = sut.getCapacity();
        final long storedAfter = sut.getStored();

        // Assert
        assertThat(capacityBefore).isEqualTo(1000);
        assertThat(storedBefore).isEqualTo(100);

        assertThat(capacityAfter).isZero();
        assertThat(storedAfter).isZero();
    }
}
