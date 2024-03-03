package com.refinedmods.refinedstorage2.api.network.impl.node.exporter;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.component.EnergyNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.node.exporter.ExporterTransferStrategy;
import com.refinedmods.refinedstorage2.api.network.node.task.TaskExecutor;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.EmptyActor;
import com.refinedmods.refinedstorage2.api.storage.InMemoryStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.InsertableStorage;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorageImpl;
import com.refinedmods.refinedstorage2.network.test.AddNetworkNode;
import com.refinedmods.refinedstorage2.network.test.InjectNetworkEnergyComponent;
import com.refinedmods.refinedstorage2.network.test.InjectNetworkStorageChannel;
import com.refinedmods.refinedstorage2.network.test.NetworkTest;
import com.refinedmods.refinedstorage2.network.test.NetworkTestFixtures;
import com.refinedmods.refinedstorage2.network.test.SetupNetwork;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage2.network.test.TestResourceKey.A;
import static com.refinedmods.refinedstorage2.network.test.TestResourceKey.B;
import static com.refinedmods.refinedstorage2.network.test.TestResourceKey.C;
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
        @InjectNetworkStorageChannel final StorageChannel storageChannel
    ) {
        // Arrange
        storageChannel.addSource(new InMemoryStorageImpl());
        storageChannel.insert(A, 100, Action.EXECUTE, EmptyActor.INSTANCE);

        final Storage destination = new LimitedStorageImpl(100);

        sut.setTransferStrategy(new CompositeExporterTransferStrategy(List.of(
            createTransferStrategy(destination, 10),
            createTransferStrategy(destination, 10),
            createTransferStrategy(destination, 10)
        )));
        sut.setFilterTemplates(List.of(A));

        // Act
        sut.doWork();

        // Assert
        assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 90)
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
        @InjectNetworkStorageChannel final StorageChannel storageChannel
    ) {
        // Arrange
        storageChannel.addSource(new InMemoryStorageImpl());
        storageChannel.insert(A, 100, Action.EXECUTE, EmptyActor.INSTANCE);
        storageChannel.insert(B, 100, Action.EXECUTE, EmptyActor.INSTANCE);

        final Storage destination = new InMemoryStorageImpl();

        sut.setFilterTemplates(List.of(A, B));
        sut.setTransferStrategy(createTransferStrategy(destination, 1));
        sut.setTaskExecutor(null);

        // Act
        sut.doWork();

        // Assert
        assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 100),
            new ResourceAmount(B, 100)
        );
        assertThat(destination.getAll()).isEmpty();
    }

    @Test
    void shouldNotTransferWithoutStrategy(@InjectNetworkStorageChannel final StorageChannel storageChannel) {
        // Arrange
        storageChannel.addSource(new InMemoryStorageImpl());
        storageChannel.insert(A, 100, Action.EXECUTE, EmptyActor.INSTANCE);
        storageChannel.insert(B, 100, Action.EXECUTE, EmptyActor.INSTANCE);

        final Storage destination = new InMemoryStorageImpl();

        sut.setFilterTemplates(List.of(A, B));

        // Act
        sut.doWork();

        // Assert
        assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 100),
            new ResourceAmount(B, 100)
        );
        assertThat(destination.getAll()).isEmpty();
    }

    @Test
    void shouldNotTransferIfInactive(@InjectNetworkStorageChannel final StorageChannel storageChannel) {
        // Arrange
        storageChannel.addSource(new InMemoryStorageImpl());
        storageChannel.insert(A, 100, Action.EXECUTE, EmptyActor.INSTANCE);
        storageChannel.insert(B, 100, Action.EXECUTE, EmptyActor.INSTANCE);

        final Storage destination = new InMemoryStorageImpl();
        final ExporterTransferStrategy strategy = createTransferStrategy(destination, 1);

        sut.setTransferStrategy(strategy);
        sut.setFilterTemplates(List.of(A, B));
        sut.setActive(false);

        // Act
        sut.doWork();

        // Assert
        assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 100),
            new ResourceAmount(B, 100)
        );
        assertThat(destination.getAll()).isEmpty();
    }

    @Test
    void shouldNotTransferWithoutTemplates(@InjectNetworkStorageChannel final StorageChannel storageChannel) {
        // Arrange
        storageChannel.addSource(new InMemoryStorageImpl());
        storageChannel.insert(A, 100, Action.EXECUTE, EmptyActor.INSTANCE);
        storageChannel.insert(B, 100, Action.EXECUTE, EmptyActor.INSTANCE);

        final Storage destination = new InMemoryStorageImpl();
        final ExporterTransferStrategy strategy = createTransferStrategy(destination, 1);

        sut.setTransferStrategy(strategy);
        sut.setFilterTemplates(List.of());

        // Act
        sut.doWork();

        // Assert
        assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 100),
            new ResourceAmount(B, 100)
        );
        assertThat(destination.getAll()).isEmpty();
    }

    @Test
    void shouldNotTransferIfNoResourcesAreAvailable(
        @InjectNetworkStorageChannel final StorageChannel storageChannel
    ) {
        // Arrange
        final Storage destination = new InMemoryStorageImpl();
        final ExporterTransferStrategy strategy = createTransferStrategy(destination, 10);

        sut.setTransferStrategy(strategy);
        sut.setFilterTemplates(List.of(A, B));

        // Act
        sut.doWork();
        sut.doWork();
        sut.doWork();

        // Assert
        assertThat(storageChannel.getAll()).isEmpty();
        assertThat(destination.getAll()).isEmpty();
    }

    @Test
    void shouldTransferWithLimitedSpaceInDestination(
        @InjectNetworkStorageChannel final StorageChannel storageChannel
    ) {
        // Arrange
        storageChannel.addSource(new InMemoryStorageImpl());
        storageChannel.insert(A, 100, Action.EXECUTE, EmptyActor.INSTANCE);
        storageChannel.insert(B, 100, Action.EXECUTE, EmptyActor.INSTANCE);
        storageChannel.insert(C, 100, Action.EXECUTE, EmptyActor.INSTANCE);

        final Storage destination = new LimitedStorageImpl(5);
        destination.insert(C, 1, Action.EXECUTE, EmptyActor.INSTANCE);

        final ExporterTransferStrategy strategy = createTransferStrategy(destination, 10);

        sut.setTransferStrategy(strategy);
        sut.setFilterTemplates(List.of(C));
        sut.setFilterTemplates(List.of(A, B));

        // Act & assert
        sut.doWork();

        assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 96),
            new ResourceAmount(B, 100),
            new ResourceAmount(C, 100)
        );
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 4),
            new ResourceAmount(C, 1)
        );

        sut.doWork();

        assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
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
        @InjectNetworkStorageChannel final StorageChannel storageChannel
    ) {
        // Arrange
        storageChannel.addSource(new InMemoryStorageImpl());
        storageChannel.insert(A, 100, Action.EXECUTE, EmptyActor.INSTANCE);
        storageChannel.insert(B, 100, Action.EXECUTE, EmptyActor.INSTANCE);

        final Storage destination = new LimitedStorageImpl(1);
        destination.insert(C, 1, Action.EXECUTE, EmptyActor.INSTANCE);

        final ExporterTransferStrategy strategy = createTransferStrategy(destination, 5);

        sut.setTransferStrategy(strategy);
        sut.setFilterTemplates(List.of(A, B));

        // Act
        sut.doWork();

        // Assert
        assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 100),
            new ResourceAmount(B, 100)
        );
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(C, 1)
        );
    }

    @Test
    void shouldTransferSingleResourceEvenIfTransferQuotaHasNotBeenMet(
        @InjectNetworkStorageChannel final StorageChannel storageChannel
    ) {
        // Arrange
        storageChannel.addSource(new InMemoryStorageImpl());
        storageChannel.insert(A, 6, Action.EXECUTE, EmptyActor.INSTANCE);
        storageChannel.insert(B, 7, Action.EXECUTE, EmptyActor.INSTANCE);

        final Storage destination = new InMemoryStorageImpl();
        final ExporterTransferStrategy strategy = createTransferStrategy(destination, 10);

        sut.setTransferStrategy(strategy);
        sut.setFilterTemplates(List.of(A, B));

        // Act
        sut.doWork();

        // Assert
        assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
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
        return new ExporterTransferStrategyImpl(destination, NetworkTestFixtures.STORAGE_CHANNEL_TYPE, transferQuota);
    }
}
