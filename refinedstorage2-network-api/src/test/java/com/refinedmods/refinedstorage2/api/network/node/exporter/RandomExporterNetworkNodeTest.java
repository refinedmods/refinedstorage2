package com.refinedmods.refinedstorage2.api.network.node.exporter;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.FixedRandomizer;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.EmptyActor;
import com.refinedmods.refinedstorage2.api.storage.InMemoryStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.network.test.InjectNetworkStorageChannel;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RandomExporterNetworkNodeTest extends AbstractExporterNetworkNodeTest {
    FixedRandomizer randomizer = new FixedRandomizer();

    @Override
    protected ExporterSchedulingMode createSchedulingMode() {
        return new RandomExporterSchedulingMode(randomizer);
    }

    @Test
    void shouldTransfer(@InjectNetworkStorageChannel final StorageChannel<String> storageChannel) {
        // Arrange
        storageChannel.addSource(new InMemoryStorageImpl<>());
        storageChannel.insert("A", 100, Action.EXECUTE, EmptyActor.INSTANCE);
        storageChannel.insert("B", 100, Action.EXECUTE, EmptyActor.INSTANCE);

        final Storage<String> destination = new InMemoryStorageImpl<>();
        final ExporterTransferStrategy strategy = createTransferStrategy(destination, 5);

        sut.setTransferStrategy(strategy);
        sut.setTemplates(List.of("A", "B"));
        randomizer.setIndex(0);

        // Act & assert
        sut.doWork();

        assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("A", 95),
            new ResourceAmount<>("B", 100)
        );
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("A", 5)
        );

        sut.doWork();

        assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("A", 90),
            new ResourceAmount<>("B", 100)
        );
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("A", 10)
        );
    }

    @Test
    void shouldWasteCycleIfResourceIsNotAvailable(
        @InjectNetworkStorageChannel final StorageChannel<String> storageChannel
    ) {
        // Arrange
        storageChannel.addSource(new InMemoryStorageImpl<>());
        storageChannel.insert("B", 7, Action.EXECUTE, EmptyActor.INSTANCE);

        final Storage<String> destination = new InMemoryStorageImpl<>();
        final ExporterTransferStrategy strategy = createTransferStrategy(destination, 10);

        sut.setTransferStrategy(strategy);
        sut.setTemplates(List.of("A", "B"));
        randomizer.setIndex(0);

        // Act & assert
        sut.doWork();

        assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("B", 7)
        );
        assertThat(destination.getAll()).isEmpty();
    }
}
