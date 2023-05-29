package com.refinedmods.refinedstorage2.api.network.impl.node;

import com.refinedmods.refinedstorage2.api.network.component.EnergyNetworkComponent;
import com.refinedmods.refinedstorage2.network.test.AddNetworkNode;
import com.refinedmods.refinedstorage2.network.test.InjectNetworkEnergyComponent;
import com.refinedmods.refinedstorage2.network.test.NetworkTest;
import com.refinedmods.refinedstorage2.network.test.SetupNetwork;

import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage2.network.test.nodefactory.AbstractNetworkNodeFactory.PROPERTY_ACTIVE;
import static com.refinedmods.refinedstorage2.network.test.nodefactory.AbstractNetworkNodeFactory.PROPERTY_ENERGY_USAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@NetworkTest
@SetupNetwork(energyStored = 10, energyCapacity = 100)
class SimpleNetworkNodeTest {
    @AddNetworkNode(properties = {
        @AddNetworkNode.Property(key = PROPERTY_ENERGY_USAGE, longValue = 10),
        @AddNetworkNode.Property(key = PROPERTY_ACTIVE, boolValue = false)
    })
    SimpleNetworkNode sut;

    @AddNetworkNode(networkId = "nonexistent")
    SimpleNetworkNode sutWithoutNetwork;

    @Test
    void testInitialState() {
        // Assert
        assertThat(sut.isActive()).isFalse();
        assertThat(sut.getNetwork()).isNotNull();
        assertThat(sut.getEnergyUsage()).isEqualTo(10);

        assertThat(sutWithoutNetwork.isActive()).isTrue();
        assertThat(sutWithoutNetwork.getNetwork()).isNull();
    }

    @Test
    void shouldNotExtractEnergyWhenInactive(@InjectNetworkEnergyComponent final EnergyNetworkComponent energy) {
        // Act
        sut.doWork();

        // Assert
        assertThat(energy.getStored()).isEqualTo(10);
    }

    @Test
    void shouldNotExtractEnergyWithoutNetwork() {
        // Act
        assertDoesNotThrow(sutWithoutNetwork::doWork);
    }

    @Test
    void shouldExtractEnergy(@InjectNetworkEnergyComponent final EnergyNetworkComponent energy) {
        // Arrange
        sut.setActive(true);

        // Act
        sut.doWork();

        // Assert
        assertThat(energy.getStored()).isZero();
    }

    @Test
    void shouldSetEnergyUsage(@InjectNetworkEnergyComponent final EnergyNetworkComponent energy) {
        // Arrange
        sut.setActive(true);
        sut.setEnergyUsage(2);

        // Act
        sut.doWork();

        // Assert
        assertThat(energy.getStored()).isEqualTo(8);
    }
}
