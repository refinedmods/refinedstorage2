package com.refinedmods.refinedstorage2.api.network.impl.node.iface;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.EmptyActor;
import com.refinedmods.refinedstorage2.api.storage.InMemoryStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.network.test.AddNetworkNode;
import com.refinedmods.refinedstorage2.network.test.InjectNetworkStorageChannel;
import com.refinedmods.refinedstorage2.network.test.NetworkTest;
import com.refinedmods.refinedstorage2.network.test.SetupNetwork;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@NetworkTest
@SetupNetwork
class ExportToEmptySlotInterfaceNetworkNodeTest {
    @AddNetworkNode
    InterfaceNetworkNode<String> sut;

    private InterfaceExportStateImpl exportState;

    @BeforeEach
    void setUp() {
        exportState = new InterfaceExportStateImpl(3);
        sut.setExportState(exportState);
        sut.setTransferQuota(2);
        sut.setEnergyUsage(5);
    }

    @Test
    void shouldNotExportToEmptySlotWhenRequestedIsNotAvailable(
        @InjectNetworkStorageChannel final StorageChannel<String> storageChannel
    ) {
        // Arrange
        exportState.setRequestedResource(1, "A", 1);
        exportState.setRequestedResource(2, "B", 1);

        // Act
        sut.doWork();

        // Assert
        assertThat(exportState.getCurrentlyExportedResource(0)).isNull();
        assertThat(exportState.getCurrentlyExportedResource(1)).isNull();
        assertThat(exportState.getCurrentlyExportedResource(2)).isNull();
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
        assertThat(exportState.getCurrentlyExportedResource(2)).isNull();

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
        assertThat(exportState.getCurrentlyExportedResource(2)).isNull();

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
        storageChannel.insert("B", 10, Action.EXECUTE, EmptyActor.INSTANCE);

        exportState.setRequestedResource(1, "A", 7);
        exportState.setRequestedResource(2, "B", 2);

        // Act
        sut.doWork();

        // Assert
        assertThat(exportState.getCurrentlyExportedResource(0)).isNull();
        assertThat(exportState.getCurrentlyExportedResource(1)).isEqualTo("A");
        assertThat(exportState.getCurrentlyExportedResourceAmount(1)).isEqualTo(2);
        assertThat(exportState.getCurrentlyExportedResource(2)).isEqualTo("B");
        assertThat(exportState.getCurrentlyExportedResourceAmount(2)).isEqualTo(2);

        assertThat(storageChannel.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount<>("A", 8),
                new ResourceAmount<>("B", 8)
            );
    }

    @Test
    void shouldExportResourceFuzzilyToEmptySlot(
        @InjectNetworkStorageChannel final StorageChannel<String> storageChannel
    ) {
        // Arrange
        storageChannel.addSource(new InMemoryStorageImpl<>());
        storageChannel.insert("A1", 10, Action.EXECUTE, EmptyActor.INSTANCE);
        storageChannel.insert("A2", 10, Action.EXECUTE, EmptyActor.INSTANCE);

        exportState.setRequestedResource(1, "A", 10);

        // Act
        sut.doWork();

        // Assert
        assertThat(exportState.getCurrentlyExportedResource(0)).isNull();
        assertThat(exportState.getCurrentlyExportedResource(1)).isEqualTo("A1");
        assertThat(exportState.getCurrentlyExportedResourceAmount(1)).isEqualTo(2);
        assertThat(exportState.getCurrentlyExportedResource(2)).isNull();

        assertThat(storageChannel.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount<>("A1", 8),
                new ResourceAmount<>("A2", 10)
            );
    }
}
