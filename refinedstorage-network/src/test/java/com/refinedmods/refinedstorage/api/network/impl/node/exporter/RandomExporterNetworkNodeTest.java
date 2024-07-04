package com.refinedmods.refinedstorage.api.network.impl.node.exporter;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.impl.node.task.RandomTaskExecutor;
import com.refinedmods.refinedstorage.api.network.node.exporter.ExporterTransferStrategy;
import com.refinedmods.refinedstorage.api.network.node.task.TaskExecutor;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.storage.EmptyActor;
import com.refinedmods.refinedstorage.api.storage.InMemoryStorageImpl;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.network.test.InjectNetworkStorageComponent;

import java.util.List;

import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage.network.test.fake.FakeResources.A;
import static com.refinedmods.refinedstorage.network.test.fake.FakeResources.B;
import static org.assertj.core.api.Assertions.assertThat;

class RandomExporterNetworkNodeTest extends AbstractExporterNetworkNodeTest {
    @Override
    protected TaskExecutor<ExporterNetworkNode.TaskContext> createTaskExecutor() {
        return new RandomTaskExecutor<>(list -> {
            list.clear();
            list.add(sut.new TaskImpl(A));
            list.add(sut.new TaskImpl(B));
        });
    }

    @Test
    void shouldTransfer(@InjectNetworkStorageComponent final StorageNetworkComponent storage) {
        // Arrange
        storage.addSource(new InMemoryStorageImpl());
        storage.insert(A, 100, Action.EXECUTE, EmptyActor.INSTANCE);
        storage.insert(B, 100, Action.EXECUTE, EmptyActor.INSTANCE);

        final Storage destination = new InMemoryStorageImpl();
        final ExporterTransferStrategy strategy = createTransferStrategy(destination, 5);

        sut.setTransferStrategy(strategy);
        sut.setFilters(List.of(B, A));

        // Act & assert
        sut.doWork();

        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 95),
            new ResourceAmount(B, 100)
        );
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 5)
        );

        sut.doWork();

        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 90),
            new ResourceAmount(B, 100)
        );
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 10)
        );
    }

    @Test
    void shouldUseNextResourceIfFirstOneIsNotAvailableInSameCycle(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage
    ) {
        // Arrange
        storage.addSource(new InMemoryStorageImpl());
        storage.insert(B, 7, Action.EXECUTE, EmptyActor.INSTANCE);

        final Storage destination = new InMemoryStorageImpl();
        final ExporterTransferStrategy strategy = createTransferStrategy(destination, 10);

        sut.setTransferStrategy(strategy);
        sut.setFilters(List.of(A, B));

        // Act & assert
        sut.doWork();

        assertThat(storage.getAll()).isEmpty();
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(B, 7)
        );

        sut.doWork();

        assertThat(storage.getAll()).isEmpty();
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(B, 7)
        );
    }
}
