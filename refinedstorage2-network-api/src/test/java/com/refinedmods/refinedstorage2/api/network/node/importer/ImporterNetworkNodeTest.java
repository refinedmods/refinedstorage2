package com.refinedmods.refinedstorage2.api.network.node.importer;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.test.extension.AddNetworkNode;
import com.refinedmods.refinedstorage2.api.network.test.extension.InjectNetworkStorageChannel;
import com.refinedmods.refinedstorage2.api.network.test.extension.NetworkTestExtension;
import com.refinedmods.refinedstorage2.api.network.test.extension.SetupNetwork;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.EmptyActor;
import com.refinedmods.refinedstorage2.api.storage.InMemoryStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorageImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(NetworkTestExtension.class)
@SetupNetwork
class ImporterNetworkNodeTest {
    @AddNetworkNode(energyUsage = 5)
    ImporterNetworkNode sut;

    @Test
    void testInitialState() {
        // Assert
        assertThat(sut.getEnergyUsage()).isEqualTo(5);
    }

    @Test
    void testTransfer(@InjectNetworkStorageChannel final StorageChannel<String> storageChannel) {
        // Arrange
        storageChannel.addSource(new InMemoryStorageImpl<>());

        final FakeSlottedImporterSource<String> source = new FakeSlottedImporterSource<>(10);
        final ImporterTransferStrategy strategy = new SlottedImporterTransferStrategy<>(
            source,
            storageChannel,
            1
        );
        sut.setTransferStrategy(strategy);

        source.setSlot(0, "A", 100);
        source.setSlot(1, "B", 100);

        // Act
        sut.update();

        // Assert
        assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("A", 1)
        );
        assertThat(source.getResourceAmount(0)).usingRecursiveComparison().isEqualTo(new ResourceAmount<>("A", 99));
        assertThat(source.getResourceAmount(1)).usingRecursiveComparison().isEqualTo(new ResourceAmount<>("B", 100));
    }

    @Test
    void testTransferWithoutSpaceInNetwork(
        @InjectNetworkStorageChannel final StorageChannel<String> storageChannel) {
        // Arrange
        storageChannel.addSource(new LimitedStorageImpl<>(100));
        storageChannel.insert("C", 100, Action.EXECUTE, EmptyActor.INSTANCE);

        final FakeSlottedImporterSource<String> source = new FakeSlottedImporterSource<>(10);
        final ImporterTransferStrategy strategy = new SlottedImporterTransferStrategy<>(
            source,
            storageChannel,
            1
        );
        sut.setTransferStrategy(strategy);

        source.setSlot(0, "A", 100);
        source.setSlot(1, "B", 100);

        // Act
        sut.update();

        // Assert
        assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("C", 100)
        );
        assertThat(source.getResourceAmount(0)).usingRecursiveComparison().isEqualTo(new ResourceAmount<>("A", 100));
        assertThat(source.getResourceAmount(1)).usingRecursiveComparison().isEqualTo(new ResourceAmount<>("B", 100));
    }

    @Test
    void testTransferOverMultipleSlots(
        @InjectNetworkStorageChannel final StorageChannel<String> storageChannel) {
        // Arrange
        storageChannel.addSource(new InMemoryStorageImpl<>());

        final FakeSlottedImporterSource<String> source = new FakeSlottedImporterSource<>(10);
        final ImporterTransferStrategy strategy = new SlottedImporterTransferStrategy<>(
            source,
            storageChannel,
            10
        );
        sut.setTransferStrategy(strategy);

        source.setSlot(0, "A", 8);
        source.setSlot(1, "B", 1);
        source.setSlot(2, "A", 4);
        source.setSlot(3, "B", 5);

        // Act
        sut.update();

        // Assert
        assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("A", 10)
        );
        assertThat(source.getResourceAmount(0)).isNull();
        assertThat(source.getResourceAmount(1)).usingRecursiveComparison().isEqualTo(new ResourceAmount<>("B", 1));
        assertThat(source.getResourceAmount(2)).usingRecursiveComparison().isEqualTo(new ResourceAmount<>("A", 2));
        assertThat(source.getResourceAmount(3)).usingRecursiveComparison().isEqualTo(new ResourceAmount<>("B", 5));
    }

    @Test
    void testTransferWhereResourceIsNotAccepted(
        @InjectNetworkStorageChannel final StorageChannel<String> storageChannel) {
        // Arrange
        storageChannel.addSource(new InMemoryStorageImpl<>() {
            @Override
            public long insert(final String resource, final long amount, final Action action, final Actor actor) {
                if ("A".equals(resource)) {
                    return 0;
                }
                return super.insert(resource, amount, action, actor);
            }
        });

        final FakeSlottedImporterSource<String> source = new FakeSlottedImporterSource<>(10);
        final ImporterTransferStrategy strategy = new SlottedImporterTransferStrategy<>(
            source,
            storageChannel,
            10
        );
        sut.setTransferStrategy(strategy);

        source.setSlot(0, "A", 8);
        source.setSlot(1, "B", 5);
        source.setSlot(2, "B", 5);
        source.setSlot(3, "B", 1);

        // Act
        sut.update();

        // Assert
        assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("B", 10)
        );
        assertThat(source.getResourceAmount(0)).usingRecursiveComparison().isEqualTo(new ResourceAmount<>("A", 8));
        assertThat(source.getResourceAmount(1)).isNull();
        assertThat(source.getResourceAmount(2)).isNull();
        assertThat(source.getResourceAmount(3)).usingRecursiveComparison().isEqualTo(new ResourceAmount<>("B", 1));
    }
}
