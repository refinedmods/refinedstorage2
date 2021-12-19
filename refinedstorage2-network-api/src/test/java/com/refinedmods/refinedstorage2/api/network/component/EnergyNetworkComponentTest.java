package com.refinedmods.refinedstorage2.api.network.component;

import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorageImpl;
import com.refinedmods.refinedstorage2.api.network.node.container.FakeNetworkNodeContainer;
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

    private NetworkNodeContainer<ControllerNetworkNode> createControllerContainer() {
        return new FakeNetworkNodeContainer<>(new ControllerNetworkNode(
                100,
                new EnergyStorageImpl(1000)
        ));
    }

    @Test
    void Test_adding_node_should_update_energy_storage() {
        // Arrange
        EnergyNetworkComponent sut = new EnergyNetworkComponent();

        long capacityBefore = sut.getEnergyStorage().getCapacity();
        long storedBefore = sut.getEnergyStorage().getStored();

        // Act
        sut.onContainerAdded(createControllerContainer());

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

        NetworkNodeContainer<ControllerNetworkNode> container = createControllerContainer();

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
