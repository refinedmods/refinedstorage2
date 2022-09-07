package com.refinedmods.refinedstorage2.api.network.node.exporter;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.EmptyActor;
import com.refinedmods.refinedstorage2.api.storage.InMemoryStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.network.test.InjectNetworkStorageChannel;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RoundRobinExporterNetworkNodeTest extends AbstractExporterNetworkNodeTest {
    @Override
    protected ExporterSchedulingMode createSchedulingMode() {
        return new RoundRobinExporterSchedulingMode();
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
            new ResourceAmount<>("A", 95),
            new ResourceAmount<>("B", 95)
        );
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("A", 5),
            new ResourceAmount<>("B", 5)
        );

        sut.doWork();

        assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("A", 90),
            new ResourceAmount<>("B", 95)
        );
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("A", 10),
            new ResourceAmount<>("B", 5)
        );
    }

    @Test
    void shouldUseNextResourceIfFirstOneIsNotAvailableInSameCycle(
        @InjectNetworkStorageChannel final StorageChannel<String> storageChannel
    ) {
        // Arrange
        storageChannel.addSource(new InMemoryStorageImpl<>());
        storageChannel.insert("C", 8, Action.EXECUTE, EmptyActor.INSTANCE);
        storageChannel.insert("D", 9, Action.EXECUTE, EmptyActor.INSTANCE);

        final Storage<String> destination = new InMemoryStorageImpl<>();
        final ExporterTransferStrategy strategy = createTransferStrategy(destination, 10);

        sut.setTransferStrategy(strategy);
        sut.setTemplates(List.of("A", "B", "C", "D"));

        // Act & assert
        sut.doWork();

        assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("D", 9)
        );
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("C", 8)
        );

        sut.doWork();

        assertThat(storageChannel.getAll()).isEmpty();
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("C", 8),
            new ResourceAmount<>("D", 9)
        );

        sut.doWork();

        assertThat(storageChannel.getAll()).isEmpty();
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("C", 8),
            new ResourceAmount<>("D", 9)
        );

        storageChannel.insert("A", 1, Action.EXECUTE, EmptyActor.INSTANCE);
        storageChannel.insert("B", 2, Action.EXECUTE, EmptyActor.INSTANCE);

        sut.doWork();

        assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("B", 2)
        );
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("A", 1),
            new ResourceAmount<>("C", 8),
            new ResourceAmount<>("D", 9)
        );

        sut.doWork();

        assertThat(storageChannel.getAll()).isEmpty();
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("A", 1),
            new ResourceAmount<>("B", 2),
            new ResourceAmount<>("C", 8),
            new ResourceAmount<>("D", 9)
        );

        sut.doWork();

        assertThat(storageChannel.getAll()).isEmpty();
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("A", 1),
            new ResourceAmount<>("B", 2),
            new ResourceAmount<>("C", 8),
            new ResourceAmount<>("D", 9)
        );
    }

    @Test
    void shouldResetRoundRobinStateAfterChangingTemplates(
        @InjectNetworkStorageChannel final StorageChannel<String> storageChannel
    ) {
        // Arrange
        storageChannel.addSource(new InMemoryStorageImpl<>());
        storageChannel.insert("A", 100, Action.EXECUTE, EmptyActor.INSTANCE);
        storageChannel.insert("B", 100, Action.EXECUTE, EmptyActor.INSTANCE);
        storageChannel.insert("C", 100, Action.EXECUTE, EmptyActor.INSTANCE);

        final Storage<String> destination = new InMemoryStorageImpl<>();
        final ExporterTransferStrategy strategy = createTransferStrategy(destination, 5);

        sut.setTransferStrategy(strategy);
        sut.setTemplates(List.of("A", "B", "C"));

        // Act & assert
        sut.doWork();

        assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("A", 95),
            new ResourceAmount<>("B", 100),
            new ResourceAmount<>("C", 100)
        );
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("A", 5)
        );

        sut.doWork();

        assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("A", 95),
            new ResourceAmount<>("B", 95),
            new ResourceAmount<>("C", 100)
        );
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("A", 5),
            new ResourceAmount<>("B", 5)
        );

        sut.setTemplates(List.of("A", "C"));
        sut.doWork();

        assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("A", 90),
            new ResourceAmount<>("B", 95),
            new ResourceAmount<>("C", 100)
        );
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("A", 10),
            new ResourceAmount<>("B", 5)
        );
    }
}
