package com.refinedmods.refinedstorage2.api.network.node.exporter;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.FixedRandomizer;
import com.refinedmods.refinedstorage2.api.network.component.EnergyNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.test.NetworkTestFixtures;
import com.refinedmods.refinedstorage2.api.network.test.extension.AddNetworkNode;
import com.refinedmods.refinedstorage2.api.network.test.extension.InjectNetworkEnergyComponent;
import com.refinedmods.refinedstorage2.api.network.test.extension.InjectNetworkStorageChannel;
import com.refinedmods.refinedstorage2.api.network.test.extension.NetworkTestExtension;
import com.refinedmods.refinedstorage2.api.network.test.extension.SetupNetwork;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.EmptyActor;
import com.refinedmods.refinedstorage2.api.storage.InMemoryStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorageImpl;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(NetworkTestExtension.class)
@SetupNetwork(energyStored = 1000, energyCapacity = 1000)
class ExporterNetworkNodeTest {
    @AddNetworkNode
    ExporterNetworkNode sut;

    @AddNetworkNode(networkId = "nonexistent")
    ExporterNetworkNode sutWithoutNetwork;

    @BeforeEach
    void setUp() {
        sut.setEnergyUsage(5);
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

        sut.setStrategyExecutor(FirstAvailableExporterTransferStrategyExecutor.INSTANCE);
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

    @Nested
    class FirstAvailable {
        @BeforeEach
        void setUp() {
            sut.setStrategyExecutor(FirstAvailableExporterTransferStrategyExecutor.INSTANCE);
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
                1
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

            // Assert
            assertThat(storageChannel.getAll()).isEmpty();
            assertThat(destination.getAll()).isEmpty();
        }
    }

    @Nested
    class RoundRobin {
        @BeforeEach
        void setUp() {
            sut.setStrategyExecutor(new RoundRobinExporterTransferStrategyExecutor());
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
                5
            );

            sut.setStrategyFactory(strategyFactory);
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

            // Act & assert
            sut.doWork();

            assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount<>("B", 7)
            );
            assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount<>("A", 6)
            );

            sut.doWork();

            assertThat(storageChannel.getAll()).isEmpty();
            assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount<>("A", 6),
                new ResourceAmount<>("B", 7)
            );
        }

        @Test
        void shouldWasteCycleIfFirstResourceIsNotAvailable(
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

            // Act & assert
            sut.doWork();

            assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount<>("B", 7)
            );
            assertThat(destination.getAll()).isEmpty();

            sut.doWork();

            assertThat(storageChannel.getAll()).isEmpty();
            assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount<>("B", 7)
            );

            sut.doWork();

            assertThat(storageChannel.getAll()).isEmpty();
            assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount<>("B", 7)
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

            final Storage<String> destination = new LimitedStorageImpl<>(9);
            destination.insert("C", 5, Action.EXECUTE, EmptyActor.INSTANCE);

            final ExporterTransferStrategyFactory strategyFactory = new ExporterTransferStrategyFactoryImpl(
                destination,
                2
            );

            sut.setStrategyFactory(strategyFactory);
            sut.setTemplates(List.of("A", "B"));

            // Act & assert
            sut.doWork();

            assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount<>("A", 98),
                new ResourceAmount<>("B", 100)
            );
            assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount<>("A", 2),
                new ResourceAmount<>("C", 5)
            );

            sut.doWork();

            assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount<>("A", 98),
                new ResourceAmount<>("B", 98)
            );
            assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount<>("A", 2),
                new ResourceAmount<>("B", 2),
                new ResourceAmount<>("C", 5)
            );

            sut.doWork();
            sut.doWork();

            assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount<>("A", 98),
                new ResourceAmount<>("B", 98)
            );
            assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount<>("A", 2),
                new ResourceAmount<>("B", 2),
                new ResourceAmount<>("C", 5)
            );
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
        void shouldResetRoundRobinStateAfterChangingTemplates(
            @InjectNetworkStorageChannel final StorageChannel<String> storageChannel
        ) {
            // Arrange
            storageChannel.addSource(new InMemoryStorageImpl<>());
            storageChannel.insert("A", 100, Action.EXECUTE, EmptyActor.INSTANCE);
            storageChannel.insert("B", 100, Action.EXECUTE, EmptyActor.INSTANCE);
            storageChannel.insert("C", 100, Action.EXECUTE, EmptyActor.INSTANCE);

            final Storage<String> destination = new InMemoryStorageImpl<>();
            final ExporterTransferStrategyFactory strategyFactory = new ExporterTransferStrategyFactoryImpl(
                destination,
                5
            );

            sut.setStrategyFactory(strategyFactory);
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

    @Nested
    class Random {
        FixedRandomizer randomizer;

        @BeforeEach
        void setUp() {
            randomizer = new FixedRandomizer();
            sut.setStrategyExecutor(new RandomExporterTransferStrategyExecutor(randomizer));
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
                5
            );

            sut.setStrategyFactory(strategyFactory);
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
            randomizer.setIndex(0);

            // Act & assert
            sut.doWork();

            assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount<>("B", 7)
            );
            assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount<>("A", 6)
            );

            sut.doWork();

            assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount<>("B", 7)
            );
            assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount<>("A", 6)
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
            final ExporterTransferStrategyFactory strategyFactory = new ExporterTransferStrategyFactoryImpl(
                destination,
                10
            );

            sut.setStrategyFactory(strategyFactory);
            sut.setTemplates(List.of("A", "B"));
            randomizer.setIndex(0);

            // Act & assert
            sut.doWork();

            assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount<>("B", 7)
            );
            assertThat(destination.getAll()).isEmpty();
        }

        @Test
        void shouldNotTransferIfThereIsNoSpaceInTheDestination(
            @InjectNetworkStorageChannel final StorageChannel<String> storageChannel
        ) {
            // Arrange
            storageChannel.addSource(new InMemoryStorageImpl<>());
            storageChannel.insert("A", 100, Action.EXECUTE, EmptyActor.INSTANCE);
            storageChannel.insert("B", 100, Action.EXECUTE, EmptyActor.INSTANCE);

            final Storage<String> destination = new LimitedStorageImpl<>(9);
            destination.insert("C", 5, Action.EXECUTE, EmptyActor.INSTANCE);

            final ExporterTransferStrategyFactory strategyFactory = new ExporterTransferStrategyFactoryImpl(
                destination,
                2
            );

            sut.setStrategyFactory(strategyFactory);
            sut.setTemplates(List.of("A", "B"));
            randomizer.setIndex(0);

            // Act & assert
            sut.doWork();

            assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount<>("A", 98),
                new ResourceAmount<>("B", 100)
            );
            assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount<>("A", 2),
                new ResourceAmount<>("C", 5)
            );

            sut.doWork();

            assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount<>("A", 96),
                new ResourceAmount<>("B", 100)
            );
            assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount<>("A", 4),
                new ResourceAmount<>("C", 5)
            );

            sut.doWork();
            sut.doWork();

            assertThat(storageChannel.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount<>("A", 96),
                new ResourceAmount<>("B", 100)
            );
            assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount<>("A", 4),
                new ResourceAmount<>("C", 5)
            );
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
            randomizer.setIndex(0);

            // Act
            sut.doWork();
            sut.doWork();
            sut.doWork();

            // Assert
            assertThat(storageChannel.getAll()).isEmpty();
            assertThat(destination.getAll()).isEmpty();
        }
    }

    private record ExporterTransferStrategyFactoryImpl(Storage<String> destination, long transferQuota)
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
