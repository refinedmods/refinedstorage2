package com.refinedmods.refinedstorage.api.network.impl.node.iface;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.energy.EnergyNetworkComponent;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.StorageImpl;
import com.refinedmods.refinedstorage.network.test.AddNetworkNode;
import com.refinedmods.refinedstorage.network.test.InjectNetworkEnergyComponent;
import com.refinedmods.refinedstorage.network.test.InjectNetworkStorageComponent;
import com.refinedmods.refinedstorage.network.test.NetworkTest;
import com.refinedmods.refinedstorage.network.test.SetupNetwork;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.A;
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
        @InjectNetworkStorageComponent final StorageNetworkComponent storage,
        @InjectNetworkEnergyComponent final EnergyNetworkComponent energy
    ) {
        // Arrange
        storage.addSource(new StorageImpl());
        storage.insert(A, Long.MAX_VALUE, Action.EXECUTE, Actor.EMPTY);

        exportState.setRequestedResource(1, A, Long.MAX_VALUE);

        // Act
        sut.doWork();

        // Assert
        assertThat(exportState.getExportedResource(0)).isNull();
        assertThat(exportState.getExportedResource(1)).isEqualTo(A);
        assertThat(exportState.getExportedAmount(1)).isEqualTo(Long.MAX_VALUE);
        assertThat(storage.getAll()).isEmpty();
        assertThat(energy.getStored()).isEqualTo(1000 - 5);
    }

    @Test
    void shouldNotExportAnythingWithoutBeingActive(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage,
        @InjectNetworkEnergyComponent final EnergyNetworkComponent energy
    ) {
        // Arrange
        storage.addSource(new StorageImpl());
        storage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        exportState.setRequestedResource(1, A, 1);

        sut.setActive(false);
        sut.setTransferQuotaProvider(resource -> 2);

        // Act
        sut.doWork();

        // Assert
        assertThat(exportState.getExportedResource(0)).isNull();
        assertThat(exportState.getExportedResource(1)).isNull();
        assertThat(storage.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new ResourceAmount(A, 10));
        assertThat(energy.getStored()).isEqualTo(1000);
    }

    @Test
    void shouldNotExportAnythingWithoutNetwork(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage,
        @InjectNetworkEnergyComponent final EnergyNetworkComponent energy
    ) {
        // Arrange
        storage.addSource(new StorageImpl());
        storage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        exportState.setRequestedResource(1, A, 1);

        sut.setNetwork(null);
        sut.setTransferQuotaProvider(resource -> 2);

        // Act
        sut.doWork();

        // Assert
        assertThat(exportState.getExportedResource(0)).isNull();
        assertThat(exportState.getExportedResource(1)).isNull();
        assertThat(storage.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new ResourceAmount(A, 10));
        assertThat(energy.getStored()).isEqualTo(1000);
    }

    @Test
    void shouldNotExportAnythingWithoutExportState(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage,
        @InjectNetworkEnergyComponent final EnergyNetworkComponent energy
    ) {
        // Arrange
        storage.addSource(new StorageImpl());
        storage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        sut.setExportState(null);
        sut.setTransferQuotaProvider(resource -> 2);

        // Act
        sut.doWork();

        // Assert
        assertThat(exportState.getExportedResource(0)).isNull();
        assertThat(exportState.getExportedResource(1)).isNull();
        assertThat(storage.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new ResourceAmount(A, 10));
        assertThat(energy.getStored()).isEqualTo(1000 - 5);
    }
}
