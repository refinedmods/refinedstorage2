package com.refinedmods.refinedstorage2.api.network.node.iface;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.component.EnergyNetworkComponent;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.EmptyActor;
import com.refinedmods.refinedstorage2.api.storage.InMemoryStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorageImpl;
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

    @Test
    void shouldNotExportToEmptySlotWhenRequestedIsNotAvailable(
        @InjectNetworkStorageChannel final StorageChannel<String> storageChannel
    ) {
        // Arrange
        exportState.setRequestedResource(1, "A", 1);

        // Act
        sut.doWork();

        // Assert
        assertThat(exportState.getCurrentlyExportedResource(0)).isNull();
        assertThat(exportState.getCurrentlyExportedResource(1)).isNull();
        assertThat(storageChannel.getAll()).isEmpty();
    }

    @Test
    void shouldExportToEmptySlotWhenRequestedIsNotEntirelyAvailable(
        @InjectNetworkStorageChannel final StorageChannel<String> storageChannel
    ) {
        // Arrange
        storageChannel.addSource(new InMemoryStorageImpl<>());
        storageChannel.insert("A", 2, Action.EXECUTE, EmptyActor.INSTANCE);

        exportState.setRequestedResource(1, "A", 10);

        sut.setTransferQuota(10);

        // Act
        sut.doWork();

        // Assert
        assertThat(exportState.getCurrentlyExportedResource(0)).isNull();
        assertThat(exportState.getCurrentlyExportedResource(1)).isEqualTo("A");
        assertThat(exportState.getCurrentlyExportedResourceAmount(1)).isEqualTo(2);

        assertThat(storageChannel.getAll()).isEmpty();
    }

    @Test
    void shouldExportToEmptySlotWhenRequestedIsLessThanTransferQuota(
        @InjectNetworkStorageChannel final StorageChannel<String> storageChannel
    ) {
        // Arrange
        storageChannel.addSource(new InMemoryStorageImpl<>());
        storageChannel.insert("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);

        exportState.setRequestedResource(1, "A", 1);

        // Act
        sut.doWork();

        // Assert
        assertThat(exportState.getCurrentlyExportedResource(0)).isNull();
        assertThat(exportState.getCurrentlyExportedResource(1)).isEqualTo("A");
        assertThat(exportState.getCurrentlyExportedResourceAmount(1)).isEqualTo(1);

        assertThat(storageChannel.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new ResourceAmount<>("A", 9));
    }

    @Test
    void shouldExportToEmptySlot(
        @InjectNetworkStorageChannel final StorageChannel<String> storageChannel
    ) {
        // Arrange
        storageChannel.addSource(new InMemoryStorageImpl<>());
        storageChannel.insert("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);

        exportState.setRequestedResource(1, "A", 7);

        // Act
        sut.doWork();

        // Assert
        assertThat(exportState.getCurrentlyExportedResource(0)).isNull();
        assertThat(exportState.getCurrentlyExportedResource(1)).isEqualTo("A");
        assertThat(exportState.getCurrentlyExportedResourceAmount(1)).isEqualTo(2);

        assertThat(storageChannel.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new ResourceAmount<>("A", 8));
    }

    @Test
    void shouldClearSlotWhenNoLongerRequestingAnything(
        @InjectNetworkStorageChannel final StorageChannel<String> storageChannel
    ) {
        // Arrange
        storageChannel.addSource(new InMemoryStorageImpl<>());

        exportState.setCurrentlyExported(1, "A", 7);

        // Act & assert
        sut.doWork();
        assertThat(exportState.getCurrentlyExportedResource(0)).isNull();
        assertThat(exportState.getCurrentlyExportedResource(1)).isEqualTo("A");
        assertThat(exportState.getCurrentlyExportedResourceAmount(1)).isEqualTo(5);
        assertThat(storageChannel.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new ResourceAmount<>("A", 2));

        sut.doWork();
        assertThat(exportState.getCurrentlyExportedResource(0)).isNull();
        assertThat(exportState.getCurrentlyExportedResource(1)).isEqualTo("A");
        assertThat(exportState.getCurrentlyExportedResourceAmount(1)).isEqualTo(3);
        assertThat(storageChannel.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new ResourceAmount<>("A", 4));

        sut.doWork();
        assertThat(exportState.getCurrentlyExportedResource(0)).isNull();
        assertThat(exportState.getCurrentlyExportedResource(1)).isEqualTo("A");
        assertThat(exportState.getCurrentlyExportedResourceAmount(1)).isEqualTo(1);
        assertThat(storageChannel.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new ResourceAmount<>("A", 6));

        sut.doWork();
        assertThat(exportState.getCurrentlyExportedResource(0)).isNull();
        assertThat(exportState.getCurrentlyExportedResource(1)).isNull();
        assertThat(storageChannel.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new ResourceAmount<>("A", 7));

        sut.doWork();
        sut.doWork();
        sut.doWork();
        assertThat(exportState.getCurrentlyExportedResource(0)).isNull();
        assertThat(exportState.getCurrentlyExportedResource(1)).isNull();
        assertThat(storageChannel.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new ResourceAmount<>("A", 7));
    }

    @Test
    void shouldClearSlotPartiallyWhenNoLongerRequestingAnythingButNetworkDoesNotHaveEnoughSpace(
        @InjectNetworkStorageChannel final StorageChannel<String> storageChannel
    ) {
        // Arrange
        storageChannel.addSource(new LimitedStorageImpl<>(3));

        exportState.setCurrentlyExported(1, "A", 7);

        // Act & assert
        sut.doWork();
        assertThat(exportState.getCurrentlyExportedResource(0)).isNull();
        assertThat(exportState.getCurrentlyExportedResource(1)).isEqualTo("A");
        assertThat(exportState.getCurrentlyExportedResourceAmount(1)).isEqualTo(5);
        assertThat(storageChannel.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new ResourceAmount<>("A", 2));

        sut.doWork();
        assertThat(exportState.getCurrentlyExportedResource(0)).isNull();
        assertThat(exportState.getCurrentlyExportedResource(1)).isEqualTo("A");
        assertThat(exportState.getCurrentlyExportedResourceAmount(1)).isEqualTo(4);
        assertThat(storageChannel.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new ResourceAmount<>("A", 3));

        sut.doWork();
        sut.doWork();
        sut.doWork();
        assertThat(exportState.getCurrentlyExportedResource(0)).isNull();
        assertThat(exportState.getCurrentlyExportedResource(1)).isEqualTo("A");
        assertThat(exportState.getCurrentlyExportedResourceAmount(1)).isEqualTo(4);
        assertThat(storageChannel.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new ResourceAmount<>("A", 3));
    }

    @Test
    void shouldNotClearSlotWhenNoLongerRequestingAnythingAndNetworkDoesNotHaveEnoughSpace(
        @InjectNetworkStorageChannel final StorageChannel<String> storageChannel
    ) {
        // Arrange
        exportState.setCurrentlyExported(1, "A", 7);

        // Act
        sut.doWork();

        // Assert
        assertThat(exportState.getCurrentlyExportedResource(0)).isNull();
        assertThat(exportState.getCurrentlyExportedResource(1)).isEqualTo("A");
        assertThat(exportState.getCurrentlyExportedResourceAmount(1)).isEqualTo(7);

        assertThat(storageChannel.getAll()).isEmpty();
    }

    @Test
    void shouldKeepExportingResourceUntilWantedAmountIsReached(
        @InjectNetworkStorageChannel final StorageChannel<String> storageChannel
    ) {
        // Arrange
        storageChannel.addSource(new InMemoryStorageImpl<>());
        storageChannel.insert("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);

        exportState.setRequestedResource(1, "A", 7);

        // Act & assert
        sut.doWork();
        assertThat(exportState.getCurrentlyExportedResource(0)).isNull();
        assertThat(exportState.getCurrentlyExportedResource(1)).isEqualTo("A");
        assertThat(exportState.getCurrentlyExportedResourceAmount(1)).isEqualTo(2);
        assertThat(storageChannel.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new ResourceAmount<>("A", 8));

        sut.doWork();
        assertThat(exportState.getCurrentlyExportedResource(0)).isNull();
        assertThat(exportState.getCurrentlyExportedResource(1)).isEqualTo("A");
        assertThat(exportState.getCurrentlyExportedResourceAmount(1)).isEqualTo(4);
        assertThat(storageChannel.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new ResourceAmount<>("A", 6));

        sut.doWork();
        assertThat(exportState.getCurrentlyExportedResource(0)).isNull();
        assertThat(exportState.getCurrentlyExportedResource(1)).isEqualTo("A");
        assertThat(exportState.getCurrentlyExportedResourceAmount(1)).isEqualTo(6);
        assertThat(storageChannel.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new ResourceAmount<>("A", 4));

        sut.doWork();
        assertThat(exportState.getCurrentlyExportedResource(0)).isNull();
        assertThat(exportState.getCurrentlyExportedResource(1)).isEqualTo("A");
        assertThat(exportState.getCurrentlyExportedResourceAmount(1)).isEqualTo(7);
        assertThat(storageChannel.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new ResourceAmount<>("A", 3));

        sut.doWork();
        sut.doWork();
        sut.doWork();
        assertThat(exportState.getCurrentlyExportedResource(0)).isNull();
        assertThat(exportState.getCurrentlyExportedResource(1)).isEqualTo("A");
        assertThat(exportState.getCurrentlyExportedResourceAmount(1)).isEqualTo(7);
        assertThat(storageChannel.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new ResourceAmount<>("A", 3));
    }

    @Test
    void shouldKeepExportingResourceUntilWantedAmountIsReachedAndNetworkHasEnoughResources(
        @InjectNetworkStorageChannel final StorageChannel<String> storageChannel
    ) {
        // Arrange
        storageChannel.addSource(new InMemoryStorageImpl<>());
        storageChannel.insert("A", 7, Action.EXECUTE, EmptyActor.INSTANCE);

        exportState.setRequestedResource(1, "A", 10);

        // Act & assert
        sut.doWork();
        assertThat(exportState.getCurrentlyExportedResource(0)).isNull();
        assertThat(exportState.getCurrentlyExportedResource(1)).isEqualTo("A");
        assertThat(exportState.getCurrentlyExportedResourceAmount(1)).isEqualTo(2);
        assertThat(storageChannel.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new ResourceAmount<>("A", 5));

        sut.doWork();
        assertThat(exportState.getCurrentlyExportedResource(0)).isNull();
        assertThat(exportState.getCurrentlyExportedResource(1)).isEqualTo("A");
        assertThat(exportState.getCurrentlyExportedResourceAmount(1)).isEqualTo(4);
        assertThat(storageChannel.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new ResourceAmount<>("A", 3));

        sut.doWork();
        assertThat(exportState.getCurrentlyExportedResource(0)).isNull();
        assertThat(exportState.getCurrentlyExportedResource(1)).isEqualTo("A");
        assertThat(exportState.getCurrentlyExportedResourceAmount(1)).isEqualTo(6);
        assertThat(storageChannel.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new ResourceAmount<>("A", 1));

        sut.doWork();
        assertThat(exportState.getCurrentlyExportedResource(0)).isNull();
        assertThat(exportState.getCurrentlyExportedResource(1)).isEqualTo("A");
        assertThat(exportState.getCurrentlyExportedResourceAmount(1)).isEqualTo(7);
        assertThat(storageChannel.getAll()).isEmpty();

        sut.doWork();
        sut.doWork();
        sut.doWork();
        assertThat(exportState.getCurrentlyExportedResource(0)).isNull();
        assertThat(exportState.getCurrentlyExportedResource(1)).isEqualTo("A");
        assertThat(exportState.getCurrentlyExportedResourceAmount(1)).isEqualTo(7);
        assertThat(storageChannel.getAll()).isEmpty();
    }

    @Test
    void shouldReturnResourceToNetworkUntilWantedAmountIsReached(
        @InjectNetworkStorageChannel final StorageChannel<String> storageChannel
    ) {
        // Arrange
        storageChannel.addSource(new InMemoryStorageImpl<>());

        exportState.setRequestedResource(1, "A", 7);
        exportState.setCurrentlyExported(1, "A", 10);

        // Act & assert
        sut.doWork();
        assertThat(exportState.getCurrentlyExportedResource(0)).isNull();
        assertThat(exportState.getCurrentlyExportedResource(1)).isEqualTo("A");
        assertThat(exportState.getCurrentlyExportedResourceAmount(1)).isEqualTo(8);
        assertThat(storageChannel.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new ResourceAmount<>("A", 2));

        sut.doWork();
        assertThat(exportState.getCurrentlyExportedResource(0)).isNull();
        assertThat(exportState.getCurrentlyExportedResource(1)).isEqualTo("A");
        assertThat(exportState.getCurrentlyExportedResourceAmount(1)).isEqualTo(7);
        assertThat(storageChannel.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new ResourceAmount<>("A", 3));

        sut.doWork();
        sut.doWork();
        sut.doWork();
        assertThat(exportState.getCurrentlyExportedResource(0)).isNull();
        assertThat(exportState.getCurrentlyExportedResource(1)).isEqualTo("A");
        assertThat(exportState.getCurrentlyExportedResourceAmount(1)).isEqualTo(7);
        assertThat(storageChannel.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new ResourceAmount<>("A", 3));
    }

    @Test
    void shouldReturnResourceToNetworkUntilWantedAmountIsReachedAndNetworkIsFull(
        @InjectNetworkStorageChannel final StorageChannel<String> storageChannel
    ) {
        // Arrange
        storageChannel.addSource(new LimitedStorageImpl<>(3));

        exportState.setRequestedResource(1, "A", 5);
        exportState.setCurrentlyExported(1, "A", 10);

        // Act & assert
        sut.doWork();
        assertThat(exportState.getCurrentlyExportedResource(0)).isNull();
        assertThat(exportState.getCurrentlyExportedResource(1)).isEqualTo("A");
        assertThat(exportState.getCurrentlyExportedResourceAmount(1)).isEqualTo(8);
        assertThat(storageChannel.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new ResourceAmount<>("A", 2));

        sut.doWork();
        assertThat(exportState.getCurrentlyExportedResource(0)).isNull();
        assertThat(exportState.getCurrentlyExportedResource(1)).isEqualTo("A");
        assertThat(exportState.getCurrentlyExportedResourceAmount(1)).isEqualTo(7);
        assertThat(storageChannel.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new ResourceAmount<>("A", 3));

        sut.doWork();
        sut.doWork();
        sut.doWork();
        assertThat(exportState.getCurrentlyExportedResource(0)).isNull();
        assertThat(exportState.getCurrentlyExportedResource(1)).isEqualTo("A");
        assertThat(exportState.getCurrentlyExportedResourceAmount(1)).isEqualTo(7);
        assertThat(storageChannel.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new ResourceAmount<>("A", 3));
    }

    @Test
    void shouldReturnResourceToNetworkAndExportOtherResourceIfSpecified(
        @InjectNetworkStorageChannel final StorageChannel<String> storageChannel
    ) {
        // Arrange
        storageChannel.addSource(new InMemoryStorageImpl<>());
        storageChannel.insert("B", 10, Action.EXECUTE, EmptyActor.INSTANCE);

        exportState.setRequestedResource(1, "B", 3);
        exportState.setCurrentlyExported(1, "A", 3);

        // Act & assert
        sut.doWork();
        assertThat(exportState.getCurrentlyExportedResource(0)).isNull();
        assertThat(exportState.getCurrentlyExportedResource(1)).isEqualTo("A");
        assertThat(exportState.getCurrentlyExportedResourceAmount(1)).isEqualTo(1);
        assertThat(storageChannel.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount<>("A", 2),
                new ResourceAmount<>("B", 10)
            );

        sut.doWork();
        assertThat(exportState.getCurrentlyExportedResource(0)).isNull();
        assertThat(exportState.getCurrentlyExportedResource(1)).isNull();
        assertThat(storageChannel.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount<>("A", 3),
                new ResourceAmount<>("B", 10)
            );

        sut.doWork();
        assertThat(exportState.getCurrentlyExportedResource(0)).isNull();
        assertThat(exportState.getCurrentlyExportedResource(1)).isEqualTo("B");
        assertThat(exportState.getCurrentlyExportedResourceAmount(1)).isEqualTo(2);
        assertThat(storageChannel.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount<>("A", 3),
                new ResourceAmount<>("B", 8)
            );

        sut.doWork();
        assertThat(exportState.getCurrentlyExportedResource(0)).isNull();
        assertThat(exportState.getCurrentlyExportedResource(1)).isEqualTo("B");
        assertThat(exportState.getCurrentlyExportedResourceAmount(1)).isEqualTo(3);
        assertThat(storageChannel.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount<>("A", 3),
                new ResourceAmount<>("B", 7)
            );

        sut.doWork();
        sut.doWork();
        sut.doWork();
        assertThat(exportState.getCurrentlyExportedResource(0)).isNull();
        assertThat(exportState.getCurrentlyExportedResource(1)).isEqualTo("B");
        assertThat(exportState.getCurrentlyExportedResourceAmount(1)).isEqualTo(3);
        assertThat(storageChannel.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount<>("A", 3),
                new ResourceAmount<>("B", 7)
            );
    }

    @Test
    void shouldReturnResourceToNetworkAndExportOtherResourceIfSpecifiedUntilNetworkIsFull(
        @InjectNetworkStorageChannel final StorageChannel<String> storageChannel
    ) {
        // Arrange
        storageChannel.addSource(new LimitedStorageImpl<>(11));
        storageChannel.insert("B", 10, Action.EXECUTE, EmptyActor.INSTANCE);

        exportState.setRequestedResource(1, "B", 3);
        exportState.setCurrentlyExported(1, "A", 3);

        // Act & assert
        sut.doWork();
        assertThat(exportState.getCurrentlyExportedResource(0)).isNull();
        assertThat(exportState.getCurrentlyExportedResource(1)).isEqualTo("A");
        assertThat(exportState.getCurrentlyExportedResourceAmount(1)).isEqualTo(2);
        assertThat(storageChannel.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount<>("A", 1),
                new ResourceAmount<>("B", 10)
            );

        sut.doWork();
        sut.doWork();
        sut.doWork();
        assertThat(exportState.getCurrentlyExportedResource(0)).isNull();
        assertThat(exportState.getCurrentlyExportedResource(1)).isEqualTo("A");
        assertThat(exportState.getCurrentlyExportedResourceAmount(1)).isEqualTo(2);
        assertThat(storageChannel.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount<>("A", 1),
                new ResourceAmount<>("B", 10)
            );
    }
}
