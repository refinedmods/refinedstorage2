package com.refinedmods.refinedstorage2.api.network.impl.node.iface;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.component.EnergyNetworkComponent;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.EmptyActor;
import com.refinedmods.refinedstorage2.api.storage.InMemoryStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.network.test.AddNetworkNode;
import com.refinedmods.refinedstorage2.network.test.InjectNetworkEnergyComponent;
import com.refinedmods.refinedstorage2.network.test.InjectNetworkStorageChannel;
import com.refinedmods.refinedstorage2.network.test.NetworkTest;
import com.refinedmods.refinedstorage2.network.test.SetupNetwork;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@NetworkTest
@SetupNetwork(energyStored = 1000, energyCapacity = 1000)
class InterfaceNetworkNodeTest {
    @AddNetworkNode
    InterfaceNetworkNode<String> sut;

    private InterfaceExportStateImpl exportState;

    @BeforeEach
    void setUp() {
        exportState = new InterfaceExportStateImpl(2);
        sut.setExportState(exportState);
        sut.setTransferQuota(2);
        sut.setEnergyUsage(5);
    }

    @Test
    void shouldExtractEnergy(
        @InjectNetworkEnergyComponent final EnergyNetworkComponent energy
    ) {
        // Act
        sut.doWork();

        // Assert
        assertThat(energy.getStored()).isEqualTo(1000 - 5);
    }

    @Test
    void shouldNotBeAbleToSetInvalidTransferQuota() {
        // Act
        final Executable action = () -> sut.setTransferQuota(-1);

        // Assert
        assertThrows(IllegalArgumentException.class, action);
    }

    @Test
    void shouldNotExportAnythingWithoutTransferQuota(
        @InjectNetworkStorageChannel final StorageChannel<String> storageChannel,
        @InjectNetworkEnergyComponent final EnergyNetworkComponent energy
    ) {
        // Arrange
        storageChannel.addSource(new InMemoryStorageImpl<>());
        storageChannel.insert("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);

        exportState.setRequestedResource(1, "A", 1);

        sut.setTransferQuota(0);

        // Act
        sut.doWork();

        // Assert
        assertThat(exportState.getCurrentlyExportedResource(0)).isNull();
        assertThat(exportState.getCurrentlyExportedResource(1)).isNull();
        assertThat(storageChannel.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new ResourceAmount<>("A", 10));
        assertThat(energy.getStored()).isEqualTo(1000 - 5);
    }

    @Test
    void shouldNotExportAnythingWithoutBeingActive(
        @InjectNetworkStorageChannel final StorageChannel<String> storageChannel,
        @InjectNetworkEnergyComponent final EnergyNetworkComponent energy
    ) {
        // Arrange
        storageChannel.addSource(new InMemoryStorageImpl<>());
        storageChannel.insert("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);

        exportState.setRequestedResource(1, "A", 1);

        sut.setActive(false);

        // Act
        sut.doWork();

        // Assert
        assertThat(exportState.getCurrentlyExportedResource(0)).isNull();
        assertThat(exportState.getCurrentlyExportedResource(1)).isNull();
        assertThat(storageChannel.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new ResourceAmount<>("A", 10));
        assertThat(energy.getStored()).isEqualTo(1000);
    }

    @Test
    void shouldNotExportAnythingWithoutNetwork(
        @InjectNetworkStorageChannel final StorageChannel<String> storageChannel,
        @InjectNetworkEnergyComponent final EnergyNetworkComponent energy
    ) {
        // Arrange
        storageChannel.addSource(new InMemoryStorageImpl<>());
        storageChannel.insert("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);

        exportState.setRequestedResource(1, "A", 1);

        sut.setNetwork(null);

        // Act
        sut.doWork();

        // Assert
        assertThat(exportState.getCurrentlyExportedResource(0)).isNull();
        assertThat(exportState.getCurrentlyExportedResource(1)).isNull();
        assertThat(storageChannel.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new ResourceAmount<>("A", 10));
        assertThat(energy.getStored()).isEqualTo(1000);
    }

    @Test
    void shouldNotExportAnythingWithoutExportState(
        @InjectNetworkStorageChannel final StorageChannel<String> storageChannel,
        @InjectNetworkEnergyComponent final EnergyNetworkComponent energy
    ) {
        // Arrange
        storageChannel.addSource(new InMemoryStorageImpl<>());
        storageChannel.insert("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);

        sut.setExportState(null);

        // Act
        sut.doWork();

        // Assert
        assertThat(exportState.getCurrentlyExportedResource(0)).isNull();
        assertThat(exportState.getCurrentlyExportedResource(1)).isNull();
        assertThat(storageChannel.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new ResourceAmount<>("A", 10));
        assertThat(energy.getStored()).isEqualTo(1000 - 5);
    }
}
