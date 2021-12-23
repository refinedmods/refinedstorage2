package com.refinedmods.refinedstorage2.api.network.node;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.NetworkImpl;
import com.refinedmods.refinedstorage2.api.network.NetworkUtil;
import com.refinedmods.refinedstorage2.api.network.component.EnergyNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorageImpl;
import com.refinedmods.refinedstorage2.test.Rs2Test;

import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage2.api.network.NetworkUtil.NETWORK_COMPONENT_REGISTRY;
import static org.assertj.core.api.Assertions.assertThat;

@Rs2Test
public class NetworkNodeImplTest {
    @Test
    void Test_activeness_with_sufficient_energy() {
        // Arrange
        FakeNetworkNode sut = new FakeNetworkNode(10);

        Network network = NetworkUtil.create(10, 100);
        sut.setNetwork(network);
        network.addContainer(() -> sut);

        // Act
        boolean active = sut.isActive();

        // Assert
        assertThat(active).isTrue();
    }

    @Test
    void Test_activeness_when_insufficient_energy() {
        // Arrange
        FakeNetworkNode sut = new FakeNetworkNode(10);

        Network network = NetworkUtil.create(9, 10);
        sut.setNetwork(network);
        network.addContainer(() -> sut);

        // Act
        boolean active = sut.isActive();

        // Assert
        assertThat(active).isFalse();
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

    @Test
    void Test_should_notify_properly_of_activeness_change() {
        // Arrange
        FakeNetworkNode sut = new FakeNetworkNode(10);

        Network network = NetworkUtil.create(30, 100);
        sut.setNetwork(network);
        network.addContainer(() -> sut);

        // Act
        // Energy stored now: 20 - Call 1 - false -> true
        sut.update();
        // Energy stored now: 10 - No call
        sut.update();
        // Energy stored now: 0 - Call 2 - true -> false
        sut.update();
        // Energy stored now: 0 - No call
        sut.update();

        // Assert
        assertThat(sut.getActivenessChanges()).isEqualTo(2);
    }
}
