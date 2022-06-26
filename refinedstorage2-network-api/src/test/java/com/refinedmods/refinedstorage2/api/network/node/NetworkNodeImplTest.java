package com.refinedmods.refinedstorage2.api.network.node;

import com.refinedmods.refinedstorage2.api.network.component.EnergyNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.test.SpyingNetworkNode;
import com.refinedmods.refinedstorage2.api.network.test.extension.AddNetworkNode;
import com.refinedmods.refinedstorage2.api.network.test.extension.InjectNetworkEnergyComponent;
import com.refinedmods.refinedstorage2.api.network.test.extension.NetworkTestExtension;
import com.refinedmods.refinedstorage2.api.network.test.extension.SetupNetwork;
import com.refinedmods.refinedstorage2.test.Rs2Test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@Rs2Test
@ExtendWith(NetworkTestExtension.class)
@SetupNetwork(energyStored = 10, energyCapacity = 100)
class NetworkNodeImplTest {
    @AddNetworkNode(energyUsage = 10)
    SpyingNetworkNode sut;

    @AddNetworkNode(energyUsage = 11)
    SpyingNetworkNode insufficientEnergyNetworkNode;

    @Test
    void Test_activeness_with_sufficient_energy() {
        // Act
        boolean active = sut.isActive();

        // Assert
        assertThat(active).isTrue();
    }

    @Test
    void Test_activeness_when_insufficient_energy() {
        // Act
        boolean active = insufficientEnergyNetworkNode.isActive();

        // Assert
        assertThat(active).isFalse();
    }

    @Test
    void Test_updating_should_extract_energy(@InjectNetworkEnergyComponent EnergyNetworkComponent energy) {
        // Act
        sut.update();

        // Assert
        assertThat(energy.getStored()).isZero();
    }

    @Test
    void Test_should_notify_properly_of_activeness_change() {
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
