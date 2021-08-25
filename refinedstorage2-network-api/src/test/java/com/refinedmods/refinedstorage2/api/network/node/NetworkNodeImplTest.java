package com.refinedmods.refinedstorage2.api.network.node;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.NetworkImpl;
import com.refinedmods.refinedstorage2.api.network.component.EnergyNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorageImpl;
import com.refinedmods.refinedstorage2.test.Rs2Test;

import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage2.api.network.NetworkUtil.NETWORK_COMPONENT_REGISTRY;
import static org.assertj.core.api.Assertions.assertThat;

@Rs2Test
public class NetworkNodeImplTest {
    @Test
    void Test_initial_activeness() {
        // Arrange
        FakeNetworkNode sut = new FakeNetworkNode(10);

        // Assert
        assertThat(sut.isActive()).isTrue();
    }

    @Test
    void Test_changing_activeness() {
        // Arrange
        FakeNetworkNode sut = new FakeNetworkNode(10);

        // Act
        sut.setActive(false);

        // Assert
        assertThat(sut.isActive()).isFalse();
    }

    @Test
    void Test_updating_should_extract_energy() {
        // Arrange
        FakeNetworkNode sut = new FakeNetworkNode(10);

        Network network = new NetworkImpl(NETWORK_COMPONENT_REGISTRY);

        network.getComponent(EnergyNetworkComponent.class)
                .getEnergyStorage()
                .addSource(new EnergyStorageImpl(100));

        network.getComponent(EnergyNetworkComponent.class)
                .getEnergyStorage()
                .receive(100L, Action.EXECUTE);

        sut.setNetwork(network);

        // Act
        sut.update();

        // Assert
        assertThat(network
                .getComponent(EnergyNetworkComponent.class)
                .getEnergyStorage()
                .getStored()).isEqualTo(90L);
    }
}
