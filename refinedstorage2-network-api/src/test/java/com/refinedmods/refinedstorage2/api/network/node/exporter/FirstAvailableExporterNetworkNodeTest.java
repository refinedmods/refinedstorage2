package com.refinedmods.refinedstorage2.api.network.node.exporter;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.EmptyActor;
import com.refinedmods.refinedstorage2.api.storage.InMemoryStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.network.test.InjectNetworkStorageChannel;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FirstAvailableExporterNetworkNodeTest extends AbstractExporterNetworkNodeTest {
    @Override
    protected ExporterTransferStrategyExecutor createStrategyExecutor() {
        return FirstAvailableExporterTransferStrategyExecutor.INSTANCE;
    }

    @Test
    void shouldTransfer(@InjectNetworkStorageChannel final StorageChannel<String> storageChannel) {
        // Arrange
        storageChannel.addSource(new InMemoryStorageImpl<>());
        storageChannel.insert("A", 100, Action.EXECUTE, EmptyActor.INSTANCE);
        storageChannel.insert("B", 100, Action.EXECUTE, EmptyActor.INSTANCE);

        final Storage<String> destination = new InMemoryStorageImpl<>();
        final ExporterTransferStrategyFactory strategyFactory = new ExporterTransferStrategyFactoryImpl(
            destination,
            1
        );

        sut.setStrategyFactory(strategyFactory);
        sut.setTemplates(List.of("A"));

        // Act
        sut.doWork();

        // Assert
        assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("A", 99),
            new ResourceAmount<>("B", 100)
        );
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("A", 1)
        );
    }

    @Test
    void shouldUseNextResourceIfFirstOneIsNotAvailableInSameCycle(
        @InjectNetworkStorageChannel final StorageChannel<String> storageChannel
    ) {
        // Arrange
        storageChannel.addSource(new InMemoryStorageImpl<>());
        storageChannel.insert("B", 7, Action.EXECUTE, EmptyActor.INSTANCE);

        final Storage<String> destination = new InMemoryStorageImpl<>();
        final ExporterTransferStrategyFactory strategyFactory = new ExporterTransferStrategyFactoryImpl(
            destination,
            10
        );

        sut.setStrategyFactory(strategyFactory);
        sut.setTemplates(List.of("A", "B"));

        // Act
        sut.doWork();

        // Assert
        assertThat(storageChannel.getAll()).isEmpty();
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("B", 7)
        );
    }

    @Test
    void shouldUseNextResourceIfFirstOneIsNotAcceptedInSameCycle(
        @InjectNetworkStorageChannel final StorageChannel<String> storageChannel
    ) {
        // Arrange
        storageChannel.addSource(new InMemoryStorageImpl<>());
        storageChannel.insert("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);
        storageChannel.insert("B", 10, Action.EXECUTE, EmptyActor.INSTANCE);
        storageChannel.insert("C", 10, Action.EXECUTE, EmptyActor.INSTANCE);

        final Storage<String> destination = new InMemoryStorageImpl<>() {
            @Override
            public long insert(final String resource, final long amount, final Action action, final Actor actor) {
                if ("A".equalsIgnoreCase(resource)) {
                    return 0;
                }
                return super.insert(resource, amount, action, actor);
            }
        };
        final ExporterTransferStrategyFactory strategyFactory = new ExporterTransferStrategyFactoryImpl(
            destination,
            20
        );

        sut.setStrategyFactory(strategyFactory);
        sut.setTemplates(List.of("A", "B", "C"));

        // Act & assert
        sut.doWork();

        assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("A", 10),
            new ResourceAmount<>("C", 10)
        );
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("B", 10)
        );

        sut.doWork();

        assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("A", 10)
        );
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("B", 10),
            new ResourceAmount<>("C", 10)
        );

        sut.doWork();

        assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("A", 10)
        );
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("B", 10),
            new ResourceAmount<>("C", 10)
        );
    }
}
