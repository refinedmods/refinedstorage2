package com.refinedmods.refinedstorage2.api.network.node.exporter;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.component.EnergyNetworkComponent;
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
import javax.annotation.Nullable;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@NetworkTest
@SetupNetwork(energyStored = 1000, energyCapacity = 1000)
abstract class AbstractExporterNetworkNodeTest {
    @AddNetworkNode
    ExporterNetworkNode sut;

    @AddNetworkNode(networkId = "nonexistent")
    ExporterNetworkNode sutWithoutNetwork;

    protected abstract ExporterSchedulingMode createSchedulingMode();

    @BeforeEach
    void setUp() {
        sut.setEnergyUsage(5);
        sut.setSchedulingMode(createSchedulingMode());
    }

    @Test
    void testInitialState() {
        // Assert
        assertThat(sut.getEnergyUsage()).isEqualTo(5);
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
    void shouldNotTransferWithoutStrategy(@InjectNetworkStorageChannel final StorageChannel<String> storageChannel) {
        // Arrange
        storageChannel.addSource(new InMemoryStorageImpl<>());
        storageChannel.insert("A", 100, Action.EXECUTE, EmptyActor.INSTANCE);
        storageChannel.insert("B", 100, Action.EXECUTE, EmptyActor.INSTANCE);

        final Storage<String> destination = new InMemoryStorageImpl<>();

        sut.setTemplates(List.of("A", "B"));

        // Act
        sut.doWork();

        // Assert
        assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("A", 100),
            new ResourceAmount<>("B", 100)
        );
        assertThat(destination.getAll()).isEmpty();
    }

    @Test
    void shouldNotTransferIfInactive(@InjectNetworkStorageChannel final StorageChannel<String> storageChannel) {
        // Arrange
        storageChannel.addSource(new InMemoryStorageImpl<>());
        storageChannel.insert("A", 100, Action.EXECUTE, EmptyActor.INSTANCE);
        storageChannel.insert("B", 100, Action.EXECUTE, EmptyActor.INSTANCE);

        final Storage<String> destination = new InMemoryStorageImpl<>();
        final ExporterTransferStrategy strategy = createTransferStrategy(destination, 1);

        sut.setTransferStrategy(strategy);
        sut.setTemplates(List.of("A", "B"));
        sut.setActive(false);

        // Act
        sut.doWork();

        // Assert
        assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("A", 100),
            new ResourceAmount<>("B", 100)
        );
        assertThat(destination.getAll()).isEmpty();
    }

    @Test
    void shouldNotTransferWithoutTemplates(@InjectNetworkStorageChannel final StorageChannel<String> storageChannel) {
        // Arrange
        storageChannel.addSource(new InMemoryStorageImpl<>());
        storageChannel.insert("A", 100, Action.EXECUTE, EmptyActor.INSTANCE);
        storageChannel.insert("B", 100, Action.EXECUTE, EmptyActor.INSTANCE);

        final Storage<String> destination = new InMemoryStorageImpl<>();
        final ExporterTransferStrategy strategy = createTransferStrategy(destination, 1);

        sut.setTransferStrategy(strategy);
        sut.setTemplates(List.of());

        // Act
        sut.doWork();

        // Assert
        assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("A", 100),
            new ResourceAmount<>("B", 100)
        );
        assertThat(destination.getAll()).isEmpty();
    }

    @Test
    void shouldNotTransferIfNoResourcesAreAvailable(
        @InjectNetworkStorageChannel final StorageChannel<String> storageChannel
    ) {
        // Arrange
        final Storage<String> destination = new InMemoryStorageImpl<>();
        final ExporterTransferStrategy strategy = createTransferStrategy(destination, 10);

        sut.setTransferStrategy(strategy);
        sut.setTemplates(List.of("A", "B"));

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
        @InjectNetworkStorageChannel final StorageChannel<String> storageChannel
    ) {
        // Arrange
        storageChannel.addSource(new InMemoryStorageImpl<>());
        storageChannel.insert("A", 100, Action.EXECUTE, EmptyActor.INSTANCE);
        storageChannel.insert("B", 100, Action.EXECUTE, EmptyActor.INSTANCE);
        storageChannel.insert("C", 100, Action.EXECUTE, EmptyActor.INSTANCE);

        final Storage<String> destination = new LimitedStorageImpl<>(5);
        destination.insert("C", 1, Action.EXECUTE, EmptyActor.INSTANCE);

        final ExporterTransferStrategy strategy = createTransferStrategy(destination, 10);

        sut.setTransferStrategy(strategy);
        sut.setTemplates(List.of("C"));
        sut.setTemplates(List.of("A", "B"));

        // Act & assert
        sut.doWork();

        assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("A", 96),
            new ResourceAmount<>("B", 100),
            new ResourceAmount<>("C", 100)
        );
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("A", 4),
            new ResourceAmount<>("C", 1)
        );

        sut.doWork();

        assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("A", 96),
            new ResourceAmount<>("B", 100),
            new ResourceAmount<>("C", 100)
        );
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("A", 4),
            new ResourceAmount<>("C", 1)
        );
    }

    @Test
    void shouldNotTransferIfThereIsNoSpaceInTheDestination(
        @InjectNetworkStorageChannel final StorageChannel<String> storageChannel
    ) {
        // Arrange
        storageChannel.addSource(new InMemoryStorageImpl<>());
        storageChannel.insert("A", 100, Action.EXECUTE, EmptyActor.INSTANCE);
        storageChannel.insert("B", 100, Action.EXECUTE, EmptyActor.INSTANCE);

        final Storage<String> destination = new LimitedStorageImpl<>(1);
        destination.insert("C", 1, Action.EXECUTE, EmptyActor.INSTANCE);

        final ExporterTransferStrategy strategy = createTransferStrategy(destination, 5);

        sut.setTransferStrategy(strategy);
        sut.setTemplates(List.of("A", "B"));

        // Act
        sut.doWork();

        // Assert
        assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("A", 100),
            new ResourceAmount<>("B", 100)
        );
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("C", 1)
        );
    }

    @Test
    void shouldTransferSingleResourceEvenIfTransferQuotaHasNotBeenMet(
        @InjectNetworkStorageChannel final StorageChannel<String> storageChannel
    ) {
        // Arrange
        storageChannel.addSource(new InMemoryStorageImpl<>());
        storageChannel.insert("A", 6, Action.EXECUTE, EmptyActor.INSTANCE);
        storageChannel.insert("B", 7, Action.EXECUTE, EmptyActor.INSTANCE);

        final Storage<String> destination = new InMemoryStorageImpl<>();
        final ExporterTransferStrategy strategy = createTransferStrategy(destination, 10);

        sut.setTransferStrategy(strategy);
        sut.setTemplates(List.of("A", "B"));

        // Act
        sut.doWork();

        // Assert
        assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("B", 7)
        );
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("A", 6)
        );
    }

    protected static ExporterTransferStrategy createTransferStrategy(
        final InsertableStorage<String> destination,
        final long transferQuota
    ) {
        return new FakeAbstractExporterTransferStrategy(destination, transferQuota);
    }

    private static class FakeAbstractExporterTransferStrategy extends AbstractExporterTransferStrategy<String> {
        private FakeAbstractExporterTransferStrategy(
            final InsertableStorage<String> destination,
            final long transferQuota
        ) {
            super(destination, NetworkTestFixtures.STORAGE_CHANNEL_TYPE, transferQuota);
        }

        @Nullable
        @Override
        protected String tryConvert(final Object resource) {
            return resource instanceof String str ? str : null;
        }
    }
}
