package com.refinedmods.refinedstorage2.api.network.component;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorageImpl;
import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage2.api.network.node.controller.ControllerNetworkNode;
import com.refinedmods.refinedstorage2.test.Rs2Test;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Rs2Test
class EnergyNetworkComponentTest {
    @Test
    void Test_initial_state() {
        // Arrange
        EnergyNetworkComponent sut = new EnergyNetworkComponent();

        // Assert
        assertThat(sut.getEnergyStorage().getStored()).isZero();
        assertThat(sut.getEnergyStorage().getCapacity()).isZero();
    }

    @Test
    void Test_adding_node_should_update_energy_storage() {
        // Arrange
        EnergyNetworkComponent sut = new EnergyNetworkComponent();

        ControllerNetworkNode controller = new ControllerNetworkNode();
        controller.setEnergyStorage(new EnergyStorageImpl(1000));
        controller.receive(100, Action.EXECUTE);
        NetworkNodeContainer container = () -> controller;

        long capacityBefore = sut.getEnergyStorage().getCapacity();
        long storedBefore = sut.getEnergyStorage().getStored();

        // Act
        sut.onContainerAdded(container);

        long capacityAfter = sut.getEnergyStorage().getCapacity();
        long storedAfter = sut.getEnergyStorage().getStored();

        // Assert
        assertThat(capacityBefore).isZero();
        assertThat(storedBefore).isZero();

        assertThat(capacityAfter).isEqualTo(1000);
        assertThat(storedAfter).isEqualTo(100);
    }

    @Test
    void Test_removing_node_should_update_energy_storage() {
        // Arrange
        EnergyNetworkComponent sut = new EnergyNetworkComponent();

        ControllerNetworkNode controller = new ControllerNetworkNode();
        controller.setEnergyStorage(new EnergyStorageImpl(1000));
        controller.receive(100, Action.EXECUTE);
        NetworkNodeContainer container = () -> controller;

        sut.onContainerAdded(container);

        long capacityBefore = sut.getEnergyStorage().getCapacity();
        long storedBefore = sut.getEnergyStorage().getStored();

        // Act
        sut.onContainerRemoved(container);

        long capacityAfter = sut.getEnergyStorage().getCapacity();
        long storedAfter = sut.getEnergyStorage().getStored();

        // Assert
        assertThat(capacityBefore).isEqualTo(1000);
        assertThat(storedBefore).isEqualTo(100);

        assertThat(capacityAfter).isZero();
        assertThat(storedAfter).isZero();
    }
}
