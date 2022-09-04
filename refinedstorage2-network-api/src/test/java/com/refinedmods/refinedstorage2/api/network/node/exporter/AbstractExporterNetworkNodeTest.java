package com.refinedmods.refinedstorage2.api.network.node.exporter;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.component.EnergyNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.test.NetworkTestFixtures;
import com.refinedmods.refinedstorage2.api.network.test.extension.AddNetworkNode;
import com.refinedmods.refinedstorage2.api.network.test.extension.InjectNetworkEnergyComponent;
import com.refinedmods.refinedstorage2.api.network.test.extension.InjectNetworkStorageChannel;
import com.refinedmods.refinedstorage2.api.network.test.extension.NetworkTestExtension;
import com.refinedmods.refinedstorage2.api.network.test.extension.SetupNetwork;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.EmptyActor;
import com.refinedmods.refinedstorage2.api.storage.InMemoryStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorageImpl;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(NetworkTestExtension.class)
@SetupNetwork(energyStored = 1000, energyCapacity = 1000)
abstract class AbstractExporterNetworkNodeTest {
    @AddNetworkNode
    ExporterNetworkNode sut;

    @AddNetworkNode(networkId = "nonexistent")
    ExporterNetworkNode sutWithoutNetwork;

    protected abstract ExporterTransferStrategyExecutor createStrategyExecutor();

    @BeforeEach
    void setUp() {
        sut.setEnergyUsage(5);
        sut.setStrategyExecutor(createStrategyExecutor());
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
    void shouldBeAbleToSetTemplatesWithoutStrategy() {
        // Arrange
        final List<Object> templates = List.of("A", "B");

        // Act
        assertDoesNotThrow(() -> sut.setTemplates(templates));
    }

    @Test
    void shouldNotTransferWithoutNetwork() {
        // Act & assert
        assertDoesNotThrow(sutWithoutNetwork::doWork);
        assertThat(sutWithoutNetwork.isActive()).isTrue();
    }

    @Test
    void shouldNotTransferIfInactive(@InjectNetworkStorageChannel final StorageChannel<String> storageChannel) {
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
        final ExporterTransferStrategyFactory strategyFactory = new ExporterTransferStrategyFactoryImpl(
            destination,
            1
        );

        sut.setStrategyFactory(strategyFactory);
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
        final ExporterTransferStrategyFactory strategyFactory = new ExporterTransferStrategyFactoryImpl(
            destination,
            10
        );

        sut.setStrategyFactory(strategyFactory);
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

        final Storage<String> destination = new LimitedStorageImpl<>(5);
        destination.insert("C", 1, Action.EXECUTE, EmptyActor.INSTANCE);

        final ExporterTransferStrategyFactory strategyFactory = new ExporterTransferStrategyFactoryImpl(
            destination,
            10
        );

        sut.setStrategyFactory(strategyFactory);
        sut.setTemplates(List.of("A", "B"));

        // Act & assert
        sut.doWork();

        assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("A", 96),
            new ResourceAmount<>("B", 100)
        );
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("A", 4),
            new ResourceAmount<>("C", 1)
        );

        sut.doWork();

        assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("A", 96),
            new ResourceAmount<>("B", 100)
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

        final ExporterTransferStrategyFactory strategyFactory = new ExporterTransferStrategyFactoryImpl(
            destination,
            5
        );

        sut.setStrategyFactory(strategyFactory);
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
        final ExporterTransferStrategyFactory strategyFactory = new ExporterTransferStrategyFactoryImpl(
            destination,
            10
        );

        sut.setStrategyFactory(strategyFactory);
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

    record ExporterTransferStrategyFactoryImpl(Storage<String> destination, long transferQuota)
        implements ExporterTransferStrategyFactory {
        @Override
        public Optional<ExporterTransferStrategy> create(final Object resource) {
            if (resource instanceof String str) {
                return Optional.of(new ExporterTransferStrategyImpl<>(
                    str,
                    destination,
                    NetworkTestFixtures.STORAGE_CHANNEL_TYPE,
                    transferQuota
                ));
            }
            return Optional.empty();
        }
    }
}
