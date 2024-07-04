package com.refinedmods.refinedstorage.api.network.impl.node.exporter;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.energy.EnergyNetworkComponent;
import com.refinedmods.refinedstorage.api.network.node.exporter.ExporterTransferStrategy;
import com.refinedmods.refinedstorage.api.network.node.task.TaskExecutor;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.EmptyActor;
import com.refinedmods.refinedstorage.api.storage.InMemoryStorageImpl;
import com.refinedmods.refinedstorage.api.storage.InsertableStorage;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.api.storage.limited.LimitedStorageImpl;
import com.refinedmods.refinedstorage.network.test.AddNetworkNode;
import com.refinedmods.refinedstorage.network.test.InjectNetworkEnergyComponent;
import com.refinedmods.refinedstorage.network.test.InjectNetworkStorageComponent;
import com.refinedmods.refinedstorage.network.test.NetworkTest;
import com.refinedmods.refinedstorage.network.test.SetupNetwork;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage.network.test.fake.FakeResources.A;
import static com.refinedmods.refinedstorage.network.test.fake.FakeResources.B;
import static com.refinedmods.refinedstorage.network.test.fake.FakeResources.C;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@NetworkTest
@SetupNetwork(energyStored = 1000, energyCapacity = 1000)
abstract class AbstractExporterNetworkNodeTest {
    @AddNetworkNode
    ExporterNetworkNode sut;

    @AddNetworkNode(networkId = "nonexistent")
    ExporterNetworkNode sutWithoutNetwork;

    protected abstract TaskExecutor<ExporterNetworkNode.TaskContext> createTaskExecutor();

    @BeforeEach
    void setUp() {
        sut.setEnergyUsage(5);
        sut.setTaskExecutor(createTaskExecutor());
    }

    @Test
    void testInitialState() {
        // Assert
        assertThat(sut.getEnergyUsage()).isEqualTo(5);
    }

    @Test
    void shouldUseFirstSuccessfulStrategy(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage
    ) {
        // Arrange
        storage.addSource(new InMemoryStorageImpl());
        storage.insert(A, 100, Action.EXECUTE, EmptyActor.INSTANCE);
        storage.insert(B, 100, Action.EXECUTE, EmptyActor.INSTANCE);

        final Storage failingDestination = new LimitedStorageImpl(0);
        final Storage destination = new LimitedStorageImpl(100);

        sut.setTransferStrategy(new CompositeExporterTransferStrategy(List.of(
            createTransferStrategy(failingDestination, 10),
            createTransferStrategy(destination, 10),
            createTransferStrategy(destination, 10)
        )));
        sut.setFilters(List.of(A, B));

        // Act
        sut.doWork();

        // Assert
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 90),
            new ResourceAmount(B, 100)
        );
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 10)
        );
    }

    @Test
    void shouldUseFirstSuccessfulResourceInTheStrategy(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage
    ) {
        // Arrange
        storage.addSource(new InMemoryStorageImpl());
        storage.insert(A, 100, Action.EXECUTE, EmptyActor.INSTANCE);
        storage.insert(B, 100, Action.EXECUTE, EmptyActor.INSTANCE);

        final Storage destination = new LimitedStorageImpl(100) {
            @Override
            public long insert(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
                if (resource != A) {
                    return 0;
                }
                return super.insert(resource, amount, action, actor);
            }
        };

        sut.setTransferStrategy(new CompositeExporterTransferStrategy(List.of(
            createTransferStrategy(destination, 10)
        )));
        sut.setFilters(List.of(B, A));

        // Act
        sut.doWork();

        // Assert
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 90),
            new ResourceAmount(B, 100)
        );
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 10)
        );
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
    void shouldNotTransferWithoutNetwork() {
        // Act & assert
        assertDoesNotThrow(sutWithoutNetwork::doWork);
        assertThat(sutWithoutNetwork.isActive()).isTrue();
    }

    @Test
    void shouldNotTransferWithoutTaskExecutor(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage
    ) {
        // Arrange
        storage.addSource(new InMemoryStorageImpl());
        storage.insert(A, 100, Action.EXECUTE, EmptyActor.INSTANCE);
        storage.insert(B, 100, Action.EXECUTE, EmptyActor.INSTANCE);

        final Storage destination = new InMemoryStorageImpl();

        sut.setFilters(List.of(A, B));
        sut.setTransferStrategy(createTransferStrategy(destination, 1));
        sut.setTaskExecutor(null);

        // Act
        sut.doWork();

        // Assert
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 100),
            new ResourceAmount(B, 100)
        );
        assertThat(destination.getAll()).isEmpty();
    }

    @Test
    void shouldNotTransferWithoutStrategy(@InjectNetworkStorageComponent final StorageNetworkComponent storage) {
        // Arrange
        storage.addSource(new InMemoryStorageImpl());
        storage.insert(A, 100, Action.EXECUTE, EmptyActor.INSTANCE);
        storage.insert(B, 100, Action.EXECUTE, EmptyActor.INSTANCE);

        final Storage destination = new InMemoryStorageImpl();

        sut.setFilters(List.of(A, B));

        // Act
        sut.doWork();

        // Assert
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 100),
            new ResourceAmount(B, 100)
        );
        assertThat(destination.getAll()).isEmpty();
    }

    @Test
    void shouldNotTransferIfInactive(@InjectNetworkStorageComponent final StorageNetworkComponent storage) {
        // Arrange
        storage.addSource(new InMemoryStorageImpl());
        storage.insert(A, 100, Action.EXECUTE, EmptyActor.INSTANCE);
        storage.insert(B, 100, Action.EXECUTE, EmptyActor.INSTANCE);

        final Storage destination = new InMemoryStorageImpl();
        final ExporterTransferStrategy strategy = createTransferStrategy(destination, 1);

        sut.setTransferStrategy(strategy);
        sut.setFilters(List.of(A, B));
        sut.setActive(false);

        // Act
        sut.doWork();

        // Assert
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 100),
            new ResourceAmount(B, 100)
        );
        assertThat(destination.getAll()).isEmpty();
    }

    @Test
    void shouldNotTransferWithoutFilters(@InjectNetworkStorageComponent final StorageNetworkComponent storage) {
        // Arrange
        storage.addSource(new InMemoryStorageImpl());
        storage.insert(A, 100, Action.EXECUTE, EmptyActor.INSTANCE);
        storage.insert(B, 100, Action.EXECUTE, EmptyActor.INSTANCE);

        final Storage destination = new InMemoryStorageImpl();
        final ExporterTransferStrategy strategy = createTransferStrategy(destination, 1);

        sut.setTransferStrategy(strategy);
        sut.setFilters(List.of());

        // Act
        sut.doWork();

        // Assert
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 100),
            new ResourceAmount(B, 100)
        );
        assertThat(destination.getAll()).isEmpty();
    }

    @Test
    void shouldNotTransferIfNoResourcesAreAvailable(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage
    ) {
        // Arrange
        final Storage destination = new InMemoryStorageImpl();
        final ExporterTransferStrategy strategy = createTransferStrategy(destination, 10);

        sut.setTransferStrategy(strategy);
        sut.setFilters(List.of(A, B));

        // Act
        sut.doWork();
        sut.doWork();
        sut.doWork();

        // Assert
        assertThat(storage.getAll()).isEmpty();
        assertThat(destination.getAll()).isEmpty();
    }

    @Test
    void shouldTransferWithLimitedSpaceInDestination(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage
    ) {
        // Arrange
        storage.addSource(new InMemoryStorageImpl());
        storage.insert(A, 100, Action.EXECUTE, EmptyActor.INSTANCE);
        storage.insert(B, 100, Action.EXECUTE, EmptyActor.INSTANCE);
        storage.insert(C, 100, Action.EXECUTE, EmptyActor.INSTANCE);

        final Storage destination = new LimitedStorageImpl(5);
        destination.insert(C, 1, Action.EXECUTE, EmptyActor.INSTANCE);

        final ExporterTransferStrategy strategy = createTransferStrategy(destination, 10);

        sut.setTransferStrategy(strategy);
        sut.setFilters(List.of(C));
        sut.setFilters(List.of(A, B));

        // Act & assert
        sut.doWork();

        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 96),
            new ResourceAmount(B, 100),
            new ResourceAmount(C, 100)
        );
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 4),
            new ResourceAmount(C, 1)
        );

        sut.doWork();

        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 96),
            new ResourceAmount(B, 100),
            new ResourceAmount(C, 100)
        );
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 4),
            new ResourceAmount(C, 1)
        );
    }

    @Test
    void shouldNotTransferIfThereIsNoSpaceInTheDestination(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage
    ) {
        // Arrange
        storage.addSource(new InMemoryStorageImpl());
        storage.insert(A, 100, Action.EXECUTE, EmptyActor.INSTANCE);
        storage.insert(B, 100, Action.EXECUTE, EmptyActor.INSTANCE);

        final Storage destination = new LimitedStorageImpl(1);
        destination.insert(C, 1, Action.EXECUTE, EmptyActor.INSTANCE);

        final ExporterTransferStrategy strategy = createTransferStrategy(destination, 5);

        sut.setTransferStrategy(strategy);
        sut.setFilters(List.of(A, B));

        // Act
        sut.doWork();

        // Assert
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 100),
            new ResourceAmount(B, 100)
        );
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(C, 1)
        );
    }

    @Test
    void shouldTransferSingleResourceEvenIfTransferQuotaHasNotBeenMet(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage
    ) {
        // Arrange
        storage.addSource(new InMemoryStorageImpl());
        storage.insert(A, 6, Action.EXECUTE, EmptyActor.INSTANCE);
        storage.insert(B, 7, Action.EXECUTE, EmptyActor.INSTANCE);

        final Storage destination = new InMemoryStorageImpl();
        final ExporterTransferStrategy strategy = createTransferStrategy(destination, 10);

        sut.setTransferStrategy(strategy);
        sut.setFilters(List.of(A, B));

        // Act
        sut.doWork();

        // Assert
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(B, 7)
        );
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 6)
        );
    }

    protected static ExporterTransferStrategy createTransferStrategy(
        final InsertableStorage destination,
        final long transferQuota
    ) {
        return new ExporterTransferStrategyImpl(destination, transferQuota);
    }
}
