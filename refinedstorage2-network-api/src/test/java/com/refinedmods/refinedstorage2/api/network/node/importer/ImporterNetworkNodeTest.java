package com.refinedmods.refinedstorage2.api.network.node.importer;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.core.filter.FilterMode;
import com.refinedmods.refinedstorage2.api.network.test.NetworkTestFixtures;
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

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(NetworkTestExtension.class)
@SetupNetwork
class ImporterNetworkNodeTest {
    @AddNetworkNode
    ImporterNetworkNode sut;

    @BeforeEach
    void setUp() {
        sut.setEnergyUsage(5);
    }

    @Test
    void testInitialState() {
        // Assert
        assertThat(sut.getEnergyUsage()).isEqualTo(5);
        assertThat(sut.getFilterMode()).isEqualTo(FilterMode.BLOCK);
    }

    @Test
    void shouldNotWorkWithoutTransferStrategy(
        @InjectNetworkStorageChannel final StorageChannel<String> storageChannel
    ) {
        // Act
        sut.doWork();

        // Assert
        assertThat(storageChannel.getAll()).isEmpty();
    }

    @Test
    void shouldNotWorkWithoutBeingActive(@InjectNetworkStorageChannel final StorageChannel<String> storageChannel) {
        // Arrange
        storageChannel.addSource(new InMemoryStorageImpl<>());

        final FakeImporterSource source = new FakeImporterSource("A", "B")
            .add("A", 100)
            .add("B", 100);
        final ImporterTransferStrategy strategy = new ImporterTransferStrategyImpl<>(
            source,
            NetworkTestFixtures.STORAGE_CHANNEL_TYPE,
            1
        );
        sut.setTransferStrategy(strategy);
        sut.setActive(false);

        // Act
        sut.doWork();

        // Assert
        assertThat(storageChannel.getAll()).isEmpty();
        assertThat(source.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("A", 100),
            new ResourceAmount<>("B", 100)
        );
    }

    @Test
    void testTransfer(@InjectNetworkStorageChannel final StorageChannel<String> storageChannel) {
        // Arrange
        storageChannel.addSource(new InMemoryStorageImpl<>());

        final FakeImporterSource source = new FakeImporterSource("A", "B", "A")
            .add("A", 100)
            .add("B", 100);
        final ImporterTransferStrategy strategy = new ImporterTransferStrategyImpl<>(
            source,
            NetworkTestFixtures.STORAGE_CHANNEL_TYPE,
            1
        );
        sut.setTransferStrategy(strategy);

        // Act
        sut.doWork();

        // Assert
        assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("A", 1)
        );
        assertThat(source.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("A", 99),
            new ResourceAmount<>("B", 100)
        );
    }

    @Test
    void testTransferWithoutSpaceInNetwork(
        @InjectNetworkStorageChannel final StorageChannel<String> storageChannel
    ) {
        // Arrange
        storageChannel.addSource(new LimitedStorageImpl<>(100));
        storageChannel.insert("C", 100, Action.EXECUTE, EmptyActor.INSTANCE);

        final FakeImporterSource source = new FakeImporterSource("A", "B")
            .add("A", 100)
            .add("B", 100);
        final ImporterTransferStrategy strategy = new ImporterTransferStrategyImpl<>(
            source,
            NetworkTestFixtures.STORAGE_CHANNEL_TYPE,
            1
        );
        sut.setTransferStrategy(strategy);

        // Act
        sut.doWork();

        // Assert
        assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("C", 100)
        );
        assertThat(source.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("A", 100),
            new ResourceAmount<>("B", 100)
        );
    }

    @Test
    void testTransferOverMultipleSlots(
        @InjectNetworkStorageChannel final StorageChannel<String> storageChannel
    ) {
        // Arrange
        storageChannel.addSource(new InMemoryStorageImpl<>());

        final FakeImporterSource source = new FakeImporterSource("A", "B", "A", "B")
            .add("A", 10)
            .add("B", 6);
        final ImporterTransferStrategy strategy = new ImporterTransferStrategyImpl<>(
            source,
            NetworkTestFixtures.STORAGE_CHANNEL_TYPE,
            10
        );
        sut.setTransferStrategy(strategy);

        // Act
        sut.doWork();

        // Assert
        assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("A", 10)
        );
        assertThat(source.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("B", 6)
        );
    }

    @Test
    void testTransferWhereResourceIsNotAccepted(
        @InjectNetworkStorageChannel final StorageChannel<String> storageChannel
    ) {
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

        final FakeImporterSource source = new FakeImporterSource("A", "B", "B", "B")
            .add("A", 8)
            .add("B", 11);
        final ImporterTransferStrategy strategy = new ImporterTransferStrategyImpl<>(
            source,
            NetworkTestFixtures.STORAGE_CHANNEL_TYPE,
            10
        );
        sut.setTransferStrategy(strategy);

        // Act
        sut.doWork();

        // Assert
        assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("B", 10)
        );
        assertThat(source.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("A", 8),
            new ResourceAmount<>("B", 1)
        );
    }

    @Test
    void testTransferWithoutAnyResourcesInSource(
        @InjectNetworkStorageChannel final StorageChannel<String> storageChannel
    ) {
        // Arrange
        storageChannel.addSource(new InMemoryStorageImpl<>());

        final FakeImporterSource source = new FakeImporterSource();
        final ImporterTransferStrategy strategy = new ImporterTransferStrategyImpl<>(
            source,
            NetworkTestFixtures.STORAGE_CHANNEL_TYPE,
            10
        );
        sut.setTransferStrategy(strategy);

        // Act
        sut.doWork();

        // Assert
        assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().isEmpty();
        assertThat(source.getAll()).isEmpty();
    }

    @Test
    void shouldRespectAllowlist(@InjectNetworkStorageChannel final StorageChannel<String> storageChannel) {
        // Arrange
        sut.setFilterMode(FilterMode.ALLOW);
        sut.setFilterTemplates(Set.of("A"));

        storageChannel.addSource(new InMemoryStorageImpl<>());

        final FakeImporterSource source = new FakeImporterSource("B", "A")
            .add("B", 10)
            .add("A", 10);

        final ImporterTransferStrategy strategy = new ImporterTransferStrategyImpl<>(
            source,
            NetworkTestFixtures.STORAGE_CHANNEL_TYPE,
            1
        );
        sut.setTransferStrategy(strategy);

        // Act
        sut.doWork();

        // Assert
        assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("A", 1)
        );
        assertThat(source.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("B", 10),
            new ResourceAmount<>("A", 9)
        );
    }

    @Test
    void shouldRespectAllowlistWithNormalizer(
        @InjectNetworkStorageChannel final StorageChannel<String> storageChannel
    ) {
        // Arrange
        sut.setFilterMode(FilterMode.ALLOW);
        sut.setFilterTemplates(Set.of("A"));
        sut.setNormalizer(value -> value instanceof String str && str.startsWith("A") ? "A" : value);

        storageChannel.addSource(new InMemoryStorageImpl<>());

        final FakeImporterSource source = new FakeImporterSource("B", "A1", "A2")
            .add("B", 10)
            .add("A1", 1)
            .add("A2", 1);

        final ImporterTransferStrategy strategy = new ImporterTransferStrategyImpl<>(
            source,
            NetworkTestFixtures.STORAGE_CHANNEL_TYPE,
            10
        );
        sut.setTransferStrategy(strategy);

        // Act
        sut.doWork();
        sut.doWork();

        // Assert
        assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("A1", 1),
            new ResourceAmount<>("A2", 1)
        );
        assertThat(source.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("B", 10)
        );
    }

    @Test
    void shouldRespectAllowlistWithoutAlternative(
        @InjectNetworkStorageChannel final StorageChannel<String> storageChannel
    ) {
        // Arrange
        sut.setFilterMode(FilterMode.ALLOW);
        sut.setFilterTemplates(Set.of("A"));

        storageChannel.addSource(new InMemoryStorageImpl<>());

        final FakeImporterSource source = new FakeImporterSource("B")
            .add("B", 10);

        final ImporterTransferStrategy strategy = new ImporterTransferStrategyImpl<>(
            source,
            NetworkTestFixtures.STORAGE_CHANNEL_TYPE,
            1
        );
        sut.setTransferStrategy(strategy);

        // Act
        sut.doWork();

        // Assert
        assertThat(storageChannel.getAll()).isEmpty();
        assertThat(source.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("B", 10)
        );
    }

    @Test
    void shouldRespectEmptyAllowlist(@InjectNetworkStorageChannel final StorageChannel<String> storageChannel) {
        // Arrange
        sut.setFilterMode(FilterMode.ALLOW);
        sut.setFilterTemplates(Set.of());

        storageChannel.addSource(new InMemoryStorageImpl<>());

        final FakeImporterSource source = new FakeImporterSource("B", "A")
            .add("B", 10)
            .add("A", 10);

        final ImporterTransferStrategy strategy = new ImporterTransferStrategyImpl<>(
            source,
            NetworkTestFixtures.STORAGE_CHANNEL_TYPE,
            1
        );
        sut.setTransferStrategy(strategy);

        // Act
        sut.doWork();

        // Assert
        assertThat(storageChannel.getAll()).isEmpty();
        assertThat(source.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("B", 10),
            new ResourceAmount<>("A", 10)
        );
    }

    @Test
    void shouldRespectBlocklist(@InjectNetworkStorageChannel final StorageChannel<String> storageChannel) {
        // Arrange
        sut.setFilterMode(FilterMode.BLOCK);
        sut.setFilterTemplates(Set.of("A"));

        storageChannel.addSource(new InMemoryStorageImpl<>());

        final FakeImporterSource source = new FakeImporterSource("A", "B")
            .add("A", 10)
            .add("B", 10);

        final ImporterTransferStrategy strategy = new ImporterTransferStrategyImpl<>(
            source,
            NetworkTestFixtures.STORAGE_CHANNEL_TYPE,
            1
        );
        sut.setTransferStrategy(strategy);

        // Act
        sut.doWork();

        // Assert
        assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("B", 1)
        );
        assertThat(source.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("A", 10),
            new ResourceAmount<>("B", 9)
        );
    }

    @Test
    void shouldRespectBlocklistWithoutAlternative(
        @InjectNetworkStorageChannel final StorageChannel<String> storageChannel
    ) {
        // Arrange
        sut.setFilterMode(FilterMode.BLOCK);
        sut.setFilterTemplates(Set.of("A"));

        storageChannel.addSource(new InMemoryStorageImpl<>());

        final FakeImporterSource source = new FakeImporterSource("A")
            .add("A", 10);

        final ImporterTransferStrategy strategy = new ImporterTransferStrategyImpl<>(
            source,
            NetworkTestFixtures.STORAGE_CHANNEL_TYPE,
            1
        );
        sut.setTransferStrategy(strategy);

        // Act
        sut.doWork();

        // Assert
        assertThat(storageChannel.getAll()).isEmpty();
        assertThat(source.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("A", 10)
        );
    }

    @Test
    void shouldRespectEmptyBlocklist(@InjectNetworkStorageChannel final StorageChannel<String> storageChannel) {
        // Arrange
        sut.setFilterMode(FilterMode.BLOCK);
        sut.setFilterTemplates(Set.of());

        storageChannel.addSource(new InMemoryStorageImpl<>());

        final FakeImporterSource source = new FakeImporterSource("A", "B")
            .add("A", 10)
            .add("B", 10);

        final ImporterTransferStrategy strategy = new ImporterTransferStrategyImpl<>(
            source,
            NetworkTestFixtures.STORAGE_CHANNEL_TYPE,
            1
        );
        sut.setTransferStrategy(strategy);

        // Act
        sut.doWork();

        // Assert
        assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("A", 1)
        );
        assertThat(source.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("A", 9),
            new ResourceAmount<>("B", 10)
        );
    }
}
