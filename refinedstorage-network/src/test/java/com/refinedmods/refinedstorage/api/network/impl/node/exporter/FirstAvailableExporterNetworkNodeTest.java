package com.refinedmods.refinedstorage.api.network.impl.node.exporter;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.impl.node.task.DefaultTaskExecutor;
import com.refinedmods.refinedstorage.api.network.node.NetworkNodeActor;
import com.refinedmods.refinedstorage.api.network.node.exporter.ExporterTransferStrategy;
import com.refinedmods.refinedstorage.api.network.node.task.TaskExecutor;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.EmptyActor;
import com.refinedmods.refinedstorage.api.storage.InMemoryStorageImpl;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage.api.storage.tracked.TrackedStorageImpl;
import com.refinedmods.refinedstorage.network.test.InjectNetworkStorageComponent;

import java.util.List;

import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage.network.test.fake.FakeResources.A;
import static com.refinedmods.refinedstorage.network.test.fake.FakeResources.B;
import static com.refinedmods.refinedstorage.network.test.fake.FakeResources.C;
import static org.assertj.core.api.Assertions.assertThat;

class FirstAvailableExporterNetworkNodeTest extends AbstractExporterNetworkNodeTest {
    @Override
    protected TaskExecutor<ExporterNetworkNode.TaskContext> createTaskExecutor() {
        return new DefaultTaskExecutor<>();
    }

    @Test
    void shouldTransfer(@InjectNetworkStorageComponent final StorageNetworkComponent storage) {
        // Arrange
        storage.addSource(new TrackedStorageImpl(new InMemoryStorageImpl(), () -> 1L));
        storage.insert(A, 100, Action.EXECUTE, EmptyActor.INSTANCE);
        storage.insert(B, 100, Action.EXECUTE, EmptyActor.INSTANCE);

        final Storage destination = new InMemoryStorageImpl();
        final ExporterTransferStrategy strategy = createTransferStrategy(destination, 1);

        sut.setTransferStrategy(strategy);
        sut.setFilters(List.of(A));

        // Act
        sut.doWork();

        // Assert
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 99),
            new ResourceAmount(B, 100)
        );
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 1)
        );
        assertThat(storage.findTrackedResourceByActorType(A, NetworkNodeActor.class))
            .get()
            .usingRecursiveComparison()
            .isEqualTo(new TrackedResource(ExporterNetworkNode.class.getName(), 1));
        assertThat(storage.findTrackedResourceByActorType(B, NetworkNodeActor.class)).isEmpty();
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

        // Act
        sut.doWork();

        // Assert
        assertThat(storage.getAll()).isEmpty();
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(B, 7)
        );
    }

    @Test
    void shouldUseNextResourceIfFirstOneIsNotAcceptedInSameCycle(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage
    ) {
        // Arrange
        storage.addSource(new InMemoryStorageImpl());
        storage.insert(A, 10, Action.EXECUTE, EmptyActor.INSTANCE);
        storage.insert(B, 10, Action.EXECUTE, EmptyActor.INSTANCE);
        storage.insert(C, 10, Action.EXECUTE, EmptyActor.INSTANCE);

        final Storage destination = new InMemoryStorageImpl() {
            @Override
            public long insert(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
                if (A.equals(resource)) {
                    return 0;
                }
                return super.insert(resource, amount, action, actor);
            }
        };
        final ExporterTransferStrategy strategy = createTransferStrategy(destination, 20);

        sut.setTransferStrategy(strategy);
        sut.setFilters(List.of(A, B, C));

        // Act & assert
        sut.doWork();

        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 10),
            new ResourceAmount(C, 10)
        );
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(B, 10)
        );

        sut.doWork();

        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 10)
        );
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(B, 10),
            new ResourceAmount(C, 10)
        );

        sut.doWork();

        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 10)
        );
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(B, 10),
            new ResourceAmount(C, 10)
        );
    }
}
