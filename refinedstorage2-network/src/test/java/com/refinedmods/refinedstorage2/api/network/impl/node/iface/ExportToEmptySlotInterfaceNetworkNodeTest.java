package com.refinedmods.refinedstorage2.api.network.impl.node.iface;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.EmptyActor;
import com.refinedmods.refinedstorage2.api.storage.InMemoryStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.ResourceTemplate;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.network.test.AddNetworkNode;
import com.refinedmods.refinedstorage2.network.test.InjectNetworkStorageChannel;
import com.refinedmods.refinedstorage2.network.test.NetworkTest;
import com.refinedmods.refinedstorage2.network.test.NetworkTestFixtures;
import com.refinedmods.refinedstorage2.network.test.SetupNetwork;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage2.network.test.TestResourceKey.A;
import static com.refinedmods.refinedstorage2.network.test.TestResourceKey.A_ALTERNATIVE;
import static com.refinedmods.refinedstorage2.network.test.TestResourceKey.A_ALTERNATIVE2;
import static com.refinedmods.refinedstorage2.network.test.TestResourceKey.B;
import static org.assertj.core.api.Assertions.assertThat;

@NetworkTest
@SetupNetwork
class ExportToEmptySlotInterfaceNetworkNodeTest {
    @AddNetworkNode
    InterfaceNetworkNode sut;

    private InterfaceExportStateImpl exportState;

    @BeforeEach
    void setUp() {
        exportState = new InterfaceExportStateImpl(3);
        sut.setExportState(exportState);
        sut.setTransferQuotaProvider(resource -> 2);
        sut.setEnergyUsage(5);
    }

    @Test
    void shouldNotExportToEmptySlotWhenRequestedIsNotAvailable(
        @InjectNetworkStorageChannel final StorageChannel storageChannel
    ) {
        // Arrange
        exportState.setRequestedResource(1, A, 1);
        exportState.setRequestedResource(2, B, 1);

        // Act
        sut.doWork();

        // Assert
        assertThat(exportState.getExportedResource(0)).isNull();
        assertThat(exportState.getExportedResource(1)).isNull();
        assertThat(exportState.getExportedResource(2)).isNull();
        assertThat(storageChannel.getAll()).isEmpty();
    }

    @Test
    void shouldExportToEmptySlotWhenRequestedIsNotEntirelyAvailable(
        @InjectNetworkStorageChannel final StorageChannel storageChannel
    ) {
        // Arrange
        storageChannel.addSource(new InMemoryStorageImpl());
        storageChannel.insert(A, 2, Action.EXECUTE, EmptyActor.INSTANCE);

        exportState.setRequestedResource(1, A, 10);

        sut.setTransferQuotaProvider(resource -> 10);

        // Act
        sut.doWork();

        // Assert
        assertThat(exportState.getExportedResource(0)).isNull();
        assertThat(exportState.getExportedResource(1)).usingRecursiveComparison().isEqualTo(
            new ResourceTemplate(A, NetworkTestFixtures.STORAGE_CHANNEL_TYPE)
        );
        assertThat(exportState.getExportedAmount(1)).isEqualTo(2);
        assertThat(exportState.getExportedResource(2)).isNull();

        assertThat(storageChannel.getAll()).isEmpty();
    }

    @Test
    void shouldExportToEmptySlotWhenRequestedIsLessThanTransferQuota(
        @InjectNetworkStorageChannel final StorageChannel storageChannel
    ) {
        // Arrange
        storageChannel.addSource(new InMemoryStorageImpl());
        storageChannel.insert(A, 10, Action.EXECUTE, EmptyActor.INSTANCE);

        exportState.setRequestedResource(1, A, 1);

        // Act
        sut.doWork();

        // Assert
        assertThat(exportState.getExportedResource(0)).isNull();
        assertThat(exportState.getExportedResource(1)).usingRecursiveComparison().isEqualTo(
            new ResourceTemplate(A, NetworkTestFixtures.STORAGE_CHANNEL_TYPE)
        );
        assertThat(exportState.getExportedAmount(1)).isEqualTo(1);
        assertThat(exportState.getExportedResource(2)).isNull();

        assertThat(storageChannel.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new ResourceAmount(A, 9));
    }

    @Test
    void shouldExportToEmptySlot(
        @InjectNetworkStorageChannel final StorageChannel storageChannel
    ) {
        // Arrange
        storageChannel.addSource(new InMemoryStorageImpl());
        storageChannel.insert(A, 10, Action.EXECUTE, EmptyActor.INSTANCE);
        storageChannel.insert(B, 10, Action.EXECUTE, EmptyActor.INSTANCE);

        exportState.setRequestedResource(1, A, 7);
        exportState.setRequestedResource(2, B, 2);

        // Act
        sut.doWork();

        // Assert
        assertThat(exportState.getExportedResource(0)).isNull();
        assertThat(exportState.getExportedResource(1)).usingRecursiveComparison().isEqualTo(
            new ResourceTemplate(A, NetworkTestFixtures.STORAGE_CHANNEL_TYPE)
        );
        assertThat(exportState.getExportedAmount(1)).isEqualTo(2);
        assertThat(exportState.getExportedResource(2)).usingRecursiveComparison().isEqualTo(
            new ResourceTemplate(B, NetworkTestFixtures.STORAGE_CHANNEL_TYPE)
        );
        assertThat(exportState.getExportedAmount(2)).isEqualTo(2);

        assertThat(storageChannel.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(A, 8),
                new ResourceAmount(B, 8)
            );
    }

    @Test
    void shouldExportResourceFuzzilyToEmptySlot(
        @InjectNetworkStorageChannel final StorageChannel storageChannel
    ) {
        // Arrange
        storageChannel.addSource(new InMemoryStorageImpl());
        storageChannel.insert(A_ALTERNATIVE, 10, Action.EXECUTE, EmptyActor.INSTANCE);
        storageChannel.insert(A_ALTERNATIVE2, 10, Action.EXECUTE, EmptyActor.INSTANCE);

        exportState.setRequestedResource(1, A, 10);

        // Act
        sut.doWork();

        // Assert
        assertThat(exportState.getExportedResource(0)).isNull();
        assertThat(exportState.getExportedResource(1)).usingRecursiveComparison().isEqualTo(
            new ResourceTemplate(A_ALTERNATIVE, NetworkTestFixtures.STORAGE_CHANNEL_TYPE)
        );
        assertThat(exportState.getExportedAmount(1)).isEqualTo(2);
        assertThat(exportState.getExportedResource(2)).isNull();

        assertThat(storageChannel.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(A_ALTERNATIVE, 8),
                new ResourceAmount(A_ALTERNATIVE2, 10)
            );
    }
}
