package com.refinedmods.refinedstorage2.api.network.node;

import com.refinedmods.refinedstorage2.api.network.component.EnergyNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.test.extension.AddNetworkNode;
import com.refinedmods.refinedstorage2.api.network.test.extension.InjectNetworkEnergyComponent;
import com.refinedmods.refinedstorage2.api.network.test.extension.NetworkTestExtension;
import com.refinedmods.refinedstorage2.api.network.test.extension.SetupNetwork;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(NetworkTestExtension.class)
@SetupNetwork(energyStored = 10, energyCapacity = 100)
class SimpleNetworkNodeTest {
    @AddNetworkNode(energyUsage = 10, active = false)
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
}
