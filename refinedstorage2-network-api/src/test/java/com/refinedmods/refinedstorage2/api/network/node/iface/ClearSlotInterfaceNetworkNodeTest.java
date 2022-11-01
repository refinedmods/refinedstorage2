package com.refinedmods.refinedstorage2.api.network.node.iface;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.InMemoryStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorageImpl;
import com.refinedmods.refinedstorage2.network.test.AddNetworkNode;
import com.refinedmods.refinedstorage2.network.test.InjectNetworkStorageChannel;
import com.refinedmods.refinedstorage2.network.test.NetworkTest;
import com.refinedmods.refinedstorage2.network.test.SetupNetwork;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@NetworkTest
@SetupNetwork
class ClearSlotInterfaceNetworkNodeTest {
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
    void shouldClearSlotWhenNoLongerRequestingAnything(
        @InjectNetworkStorageChannel final StorageChannel<String> storageChannel
    ) {
        // Arrange
        storageChannel.addSource(new InMemoryStorageImpl<>());

        exportState.setCurrentlyExported(1, "A", 7);
        exportState.setCurrentlyExported(2, "B", 2);

        // Act & assert
        sut.doWork();
        assertThat(exportState.getCurrentlyExportedResource(0)).isNull();
        assertThat(exportState.getCurrentlyExportedResource(1)).isEqualTo("A");
        assertThat(exportState.getCurrentlyExportedResourceAmount(1)).isEqualTo(5);
        assertThat(exportState.getCurrentlyExportedResource(2)).isNull();
        assertThat(storageChannel.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount<>("A", 2),
                new ResourceAmount<>("B", 2)
            );

        sut.doWork();
        assertThat(exportState.getCurrentlyExportedResource(0)).isNull();
        assertThat(exportState.getCurrentlyExportedResource(1)).isEqualTo("A");
        assertThat(exportState.getCurrentlyExportedResourceAmount(1)).isEqualTo(3);
        assertThat(exportState.getCurrentlyExportedResource(2)).isNull();
        assertThat(storageChannel.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount<>("A", 4),
                new ResourceAmount<>("B", 2)
            );

        sut.doWork();
        assertThat(exportState.getCurrentlyExportedResource(0)).isNull();
        assertThat(exportState.getCurrentlyExportedResource(1)).isEqualTo("A");
        assertThat(exportState.getCurrentlyExportedResourceAmount(1)).isEqualTo(1);
        assertThat(exportState.getCurrentlyExportedResource(2)).isNull();
        assertThat(storageChannel.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount<>("A", 6),
                new ResourceAmount<>("B", 2)
            );

        sut.doWork();
        assertThat(exportState.getCurrentlyExportedResource(0)).isNull();
        assertThat(exportState.getCurrentlyExportedResource(1)).isNull();
        assertThat(exportState.getCurrentlyExportedResource(2)).isNull();
        assertThat(storageChannel.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount<>("A", 7),
                new ResourceAmount<>("B", 2)
            );

        sut.doWork();
        sut.doWork();
        sut.doWork();
        assertThat(exportState.getCurrentlyExportedResource(0)).isNull();
        assertThat(exportState.getCurrentlyExportedResource(1)).isNull();
        assertThat(exportState.getCurrentlyExportedResource(2)).isNull();
        assertThat(storageChannel.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount<>("A", 7),
                new ResourceAmount<>("B", 2)
            );
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
}
