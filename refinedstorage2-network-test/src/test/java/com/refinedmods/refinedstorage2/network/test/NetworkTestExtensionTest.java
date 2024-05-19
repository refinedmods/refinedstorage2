package com.refinedmods.refinedstorage2.network.test;

import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.energy.EnergyNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.impl.node.SimpleNetworkNode;
import com.refinedmods.refinedstorage2.api.network.impl.node.storage.StorageNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.GraphNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage2.api.network.security.SecurityNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.network.test.nodefactory.AbstractNetworkNodeFactory;
import com.refinedmods.refinedstorage2.network.test.nodefactory.SimpleNetworkNodeFactory;
import com.refinedmods.refinedstorage2.network.test.nodefactory.StorageNetworkNodeFactory;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith({NetworkTestExtension.class})
@SetupNetwork(id = "a", energyCapacity = 100, energyStored = 50)
@SetupNetwork(id = "b")
@RegisterNetworkNode(value = StorageNetworkNodeFactory.class, clazz = StorageNetworkNode.class)
@RegisterNetworkNode(value = StorageNetworkNodeFactory.class, clazz = StorageNetworkNode.class)
@RegisterNetworkNode(value = SimpleNetworkNodeFactory.class, clazz = SimpleNetworkNode.class)
class NetworkTestExtensionTest {
    @InjectNetwork("a")
    Network a;

    @InjectNetwork("b")
    Network b;

    @AddNetworkNode(networkId = "a", properties = {
        @AddNetworkNode.Property(key = AbstractNetworkNodeFactory.PROPERTY_ENERGY_USAGE, longValue = 10)
    })
    StorageNetworkNode storageInA;

    @AddNetworkNode(networkId = "b", properties = {
        @AddNetworkNode.Property(key = AbstractNetworkNodeFactory.PROPERTY_ACTIVE, boolValue = false)
    })
    StorageNetworkNode storageInB;

    @AddNetworkNode(networkId = "nonexistent")
    SimpleNetworkNode nonexistentNetworkNode;

    @Test
    void shouldInjectNetworkThroughField() {
        // Assert
        assertThat(a).isNotNull();
        assertThat(b).isNotNull();
        assertThat(a).isNotSameAs(b);
    }

    @Test
    void shouldSetNetwork() {
        // Assert
        assertThat(nonexistentNetworkNode.getNetwork()).isNull();
        assertThat(storageInA.getNetwork()).isEqualTo(a);
        assertThat(storageInB.getNetwork()).isEqualTo(b);
    }

    @Test
    @SetupNetwork(id = "method_test")
    void shouldSetNetworkThroughMethod(
        @InjectNetwork("method_test") final Network network
    ) {
        // Assert
        assertThat(network)
            .isNotNull()
            .isNotEqualTo(a)
            .isNotEqualTo(b);
    }

    @Test
    @SetupNetwork(id = "without_energy", setupEnergy = false)
    void shouldNotSetupEnergy(
        @InjectNetworkEnergyComponent(networkId = "without_energy") final EnergyNetworkComponent energy
    ) {
        // Assert
        assertThat(energy.getStored()).isZero();
        assertThat(energy.getCapacity()).isZero();
        assertThat(energy.extract(1)).isZero();
    }

    @Test
    void shouldSetActivenessOfNetworkNode() {
        // Assert
        assertThat(storageInA.isActive()).isTrue();
        assertThat(storageInB.isActive()).isFalse();
    }

    @Test
    void shouldSetEnergyUsage() {
        // Assert
        assertThat(storageInA.getEnergyUsage()).isEqualTo(10);
        assertThat(storageInB.getEnergyUsage()).isZero();
    }

    @Test
    void shouldSetEnergyComponent() {
        // Arrange
        final EnergyNetworkComponent energy = a.getComponent(EnergyNetworkComponent.class);

        // Assert
        assertThat(energy.getCapacity()).isEqualTo(100L);
        assertThat(energy.getStored()).isEqualTo(50L);
    }

    @Test
    void shouldLoadNetworkNode() {
        // Assert
        assertThat(storageInA).isNotNull();
        assertThat(storageInB).isNotNull();
    }

    @Test
    void shouldAddNetworkNodeToGraph() {
        // Assert
        assertThat(a.getComponent(GraphNetworkComponent.class).getContainers())
            .extracting(NetworkNodeContainer::getNode)
            .containsExactly(storageInA);

        assertThat(b.getComponent(GraphNetworkComponent.class).getContainers())
            .extracting(NetworkNodeContainer::getNode)
            .containsExactly(storageInB);
    }

    @Test
    void shouldInjectNetworkStorageComponent(
        @InjectNetworkStorageComponent(networkId = "a") final StorageNetworkComponent networkStorageA,
        @InjectNetworkStorageComponent(networkId = "b") final StorageNetworkComponent networkStorageB
    ) {
        // Assert
        assertThat(networkStorageA).isSameAs(a.getComponent(StorageNetworkComponent.class));
        assertThat(networkStorageB).isSameAs(b.getComponent(StorageNetworkComponent.class));
    }

    @Test
    void shouldInjectNetworkEnergyComponent(
        @InjectNetworkEnergyComponent(networkId = "a") final EnergyNetworkComponent networkEnergyA,
        @InjectNetworkEnergyComponent(networkId = "b") final EnergyNetworkComponent networkEnergyB
    ) {
        // Assert
        assertThat(networkEnergyA).isSameAs(a.getComponent(EnergyNetworkComponent.class));
        assertThat(networkEnergyB).isSameAs(b.getComponent(EnergyNetworkComponent.class));
    }

    @Test
    void shouldInjectNetworkSecurityComponent(
        @InjectNetworkSecurityComponent(networkId = "a") final SecurityNetworkComponent networkSecurityA,
        @InjectNetworkSecurityComponent(networkId = "b") final SecurityNetworkComponent networkSecurityB
    ) {
        // Assert
        assertThat(networkSecurityA).isSameAs(a.getComponent(SecurityNetworkComponent.class));
        assertThat(networkSecurityB).isSameAs(b.getComponent(SecurityNetworkComponent.class));
    }

    @Test
    void shouldInjectNetworkThroughParameter(
        @InjectNetwork("a") final Network injectedA,
        @InjectNetwork("b") final Network injectedB
    ) {
        // Assert
        assertThat(injectedA).isSameAs(a);
        assertThat(injectedB).isSameAs(b);
    }

    @Nested
    @SetupNetwork(id = "c")
    class NestedTest {
        @InjectNetwork("c")
        Network nestedNetwork;

        @AddNetworkNode(networkId = "a")
        StorageNetworkNode nodeInA;

        @Test
        void testNestedNetworkAndNestedNetworkNode() {
            assertThat(nestedNetwork).isNotNull();
            assertThat(nodeInA).isNotNull();
            assertThat(a.getComponent(GraphNetworkComponent.class).getContainers())
                .extracting(NetworkNodeContainer::getNode)
                .containsExactlyInAnyOrder(storageInA, nodeInA);
        }
    }
}
