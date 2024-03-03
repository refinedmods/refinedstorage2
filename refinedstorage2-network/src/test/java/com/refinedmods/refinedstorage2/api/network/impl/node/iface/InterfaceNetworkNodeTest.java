package com.refinedmods.refinedstorage2.api.network.impl.node.iface;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.component.EnergyNetworkComponent;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.EmptyActor;
import com.refinedmods.refinedstorage2.api.storage.InMemoryStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.ResourceTemplate;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.network.test.AddNetworkNode;
import com.refinedmods.refinedstorage2.network.test.InjectNetworkEnergyComponent;
import com.refinedmods.refinedstorage2.network.test.InjectNetworkStorageChannel;
import com.refinedmods.refinedstorage2.network.test.NetworkTest;
import com.refinedmods.refinedstorage2.network.test.NetworkTestFixtures;
import com.refinedmods.refinedstorage2.network.test.SetupNetwork;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage2.network.test.TestResourceKey.A;
import static org.assertj.core.api.Assertions.assertThat;

@NetworkTest
@SetupNetwork(energyStored = 1000, energyCapacity = 1000)
class InterfaceNetworkNodeTest {
    @AddNetworkNode
    InterfaceNetworkNode sut;

    private InterfaceExportStateImpl exportState;

    @BeforeEach
    void setUp() {
        exportState = new InterfaceExportStateImpl(2);
        sut.setExportState(exportState);
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
    void shouldExportAllWithDefaultTransferQuota(
        @InjectNetworkStorageChannel final StorageChannel storageChannel,
        @InjectNetworkEnergyComponent final EnergyNetworkComponent energy
    ) {
        // Arrange
        storageChannel.addSource(new InMemoryStorageImpl());
        storageChannel.insert(A, Long.MAX_VALUE, Action.EXECUTE, EmptyActor.INSTANCE);

        exportState.setRequestedResource(1, A, Long.MAX_VALUE);

        // Act
        sut.doWork();

        // Assert
        assertThat(exportState.getExportedResource(0)).isNull();
        assertThat(exportState.getExportedResource(1)).usingRecursiveComparison().isEqualTo(
            new ResourceTemplate(A, NetworkTestFixtures.STORAGE_CHANNEL_TYPE)
        );
        assertThat(exportState.getExportedAmount(1)).isEqualTo(Long.MAX_VALUE);
        assertThat(storageChannel.getAll()).isEmpty();
        assertThat(energy.getStored()).isEqualTo(1000 - 5);
    }

    @Test
    void shouldNotExportAnythingWithoutBeingActive(
        @InjectNetworkStorageChannel final StorageChannel storageChannel,
        @InjectNetworkEnergyComponent final EnergyNetworkComponent energy
    ) {
        // Arrange
        storageChannel.addSource(new InMemoryStorageImpl());
        storageChannel.insert(A, 10, Action.EXECUTE, EmptyActor.INSTANCE);

        exportState.setRequestedResource(1, A, 1);

        sut.setActive(false);
        sut.setTransferQuotaProvider(resource -> 2);

        // Act
        sut.doWork();

        // Assert
        assertThat(exportState.getExportedResource(0)).isNull();
        assertThat(exportState.getExportedResource(1)).isNull();
        assertThat(storageChannel.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new ResourceAmount(A, 10));
        assertThat(energy.getStored()).isEqualTo(1000);
    }

    @Test
    void shouldNotExportAnythingWithoutNetwork(
        @InjectNetworkStorageChannel final StorageChannel storageChannel,
        @InjectNetworkEnergyComponent final EnergyNetworkComponent energy
    ) {
        // Arrange
        storageChannel.addSource(new InMemoryStorageImpl());
        storageChannel.insert(A, 10, Action.EXECUTE, EmptyActor.INSTANCE);

        exportState.setRequestedResource(1, A, 1);

        sut.setNetwork(null);
        sut.setTransferQuotaProvider(resource -> 2);

        // Act
        sut.doWork();

        // Assert
        assertThat(exportState.getExportedResource(0)).isNull();
        assertThat(exportState.getExportedResource(1)).isNull();
        assertThat(storageChannel.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new ResourceAmount(A, 10));
        assertThat(energy.getStored()).isEqualTo(1000);
    }

    @Test
    void shouldNotExportAnythingWithoutExportState(
        @InjectNetworkStorageChannel final StorageChannel storageChannel,
        @InjectNetworkEnergyComponent final EnergyNetworkComponent energy
    ) {
        // Arrange
        storageChannel.addSource(new InMemoryStorageImpl());
        storageChannel.insert(A, 10, Action.EXECUTE, EmptyActor.INSTANCE);

        sut.setExportState(null);
        sut.setTransferQuotaProvider(resource -> 2);

        // Act
        sut.doWork();

        // Assert
        assertThat(exportState.getExportedResource(0)).isNull();
        assertThat(exportState.getExportedResource(1)).isNull();
        assertThat(storageChannel.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new ResourceAmount(A, 10));
        assertThat(energy.getStored()).isEqualTo(1000 - 5);
    }
}
