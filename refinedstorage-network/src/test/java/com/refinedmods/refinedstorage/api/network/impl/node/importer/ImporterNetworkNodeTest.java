package com.refinedmods.refinedstorage.api.network.impl.node.importer;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.energy.EnergyNetworkComponent;
import com.refinedmods.refinedstorage.api.network.node.importer.ImporterTransferStrategy;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.filter.FilterMode;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.EmptyActor;
import com.refinedmods.refinedstorage.api.storage.InMemoryStorageImpl;
import com.refinedmods.refinedstorage.api.storage.limited.LimitedStorageImpl;
import com.refinedmods.refinedstorage.network.test.AddNetworkNode;
import com.refinedmods.refinedstorage.network.test.InjectNetworkEnergyComponent;
import com.refinedmods.refinedstorage.network.test.InjectNetworkStorageComponent;
import com.refinedmods.refinedstorage.network.test.NetworkTest;
import com.refinedmods.refinedstorage.network.test.SetupNetwork;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage.network.test.fake.FakeResources.A;
import static com.refinedmods.refinedstorage.network.test.fake.FakeResources.A_ALTERNATIVE;
import static com.refinedmods.refinedstorage.network.test.fake.FakeResources.A_ALTERNATIVE2;
import static com.refinedmods.refinedstorage.network.test.fake.FakeResources.B;
import static com.refinedmods.refinedstorage.network.test.fake.FakeResources.C;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@NetworkTest
@SetupNetwork(energyStored = 1000, energyCapacity = 1000)
class ImporterNetworkNodeTest {
    @AddNetworkNode
    ImporterNetworkNode sut;

    @AddNetworkNode(networkId = "nonexistent")
    ImporterNetworkNode sutWithoutNetwork;

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
    void shouldExtractEnergy(
        @InjectNetworkEnergyComponent final EnergyNetworkComponent energy
    ) {
        // Act
        sut.doWork();

        // Assert
        assertThat(energy.getStored()).isEqualTo(1000 - 5);
    }

    @Test
    void shouldNotWorkWithoutAnyTransferStrategy(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage,
        @InjectNetworkEnergyComponent final EnergyNetworkComponent energy
    ) {
        // Act
        sut.doWork();

        // Assert
        assertThat(storage.getAll()).isEmpty();
        assertThat(energy.getStored()).isEqualTo(1000 - 5);
    }

    @Test
    void shouldNotWorkWithoutNetwork() {
        // Act & assert
        assertDoesNotThrow(sutWithoutNetwork::doWork);
        assertThat(sutWithoutNetwork.isActive()).isTrue();
    }

    @Test
    void shouldNotWorkOrExtractEnergyWithoutBeingActive(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage,
        @InjectNetworkEnergyComponent final EnergyNetworkComponent energy
    ) {
        // Arrange
        storage.addSource(new InMemoryStorageImpl());

        final FakeImporterSource source = new FakeImporterSource(A, B)
            .add(A, 100)
            .add(B, 100);
        final ImporterTransferStrategy strategy = new ImporterTransferStrategyImpl(source, 1);
        sut.setTransferStrategies(List.of(strategy));
        sut.setActive(false);

        // Act
        sut.doWork();

        // Assert
        assertThat(storage.getAll()).isEmpty();
        assertThat(source.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 100),
            new ResourceAmount(B, 100)
        );
        assertThat(energy.getStored()).isEqualTo(1000);
    }

    @Test
    void testTransfer(@InjectNetworkStorageComponent final StorageNetworkComponent storage) {
        // Arrange
        storage.addSource(new InMemoryStorageImpl());

        final FakeImporterSource source = new FakeImporterSource(A, B, A)
            .add(A, 100)
            .add(B, 100);
        final ImporterTransferStrategy strategy = new ImporterTransferStrategyImpl(source, 1);
        sut.setTransferStrategies(List.of(strategy));

        // Act
        sut.doWork();

        // Assert
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 1)
        );
        assertThat(source.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 99),
            new ResourceAmount(B, 100)
        );
    }

    @Test
    void shouldUseFirstSuccessfulTransferStrategy(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage
    ) {
        // Arrange
        storage.addSource(new InMemoryStorageImpl());

        final FakeImporterSource emptySource = new FakeImporterSource();
        final FakeImporterSource outdatedSource = new FakeImporterSource(C)
            .add(C, 100);
        final FakeImporterSource source = new FakeImporterSource(A, B, A)
            .add(A, 100)
            .add(B, 100);

        sut.setTransferStrategies(List.of(
            new ImporterTransferStrategyImpl(outdatedSource, 1)
        ));
        sut.setTransferStrategies(List.of(
            new ImporterTransferStrategyImpl(emptySource, 1),
            new ImporterTransferStrategyImpl(source, 1),
            new ImporterTransferStrategyImpl(source, 1)
        ));

        // Act
        sut.doWork();

        // Assert
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 1)
        );
        assertThat(source.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 99),
            new ResourceAmount(B, 100)
        );
    }

    @Test
    void shouldNotTransferIfThereIsNoSpaceInTheNetwork(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage
    ) {
        // Arrange
        storage.addSource(new LimitedStorageImpl(100));
        storage.insert(C, 100, Action.EXECUTE, EmptyActor.INSTANCE);

        final FakeImporterSource source = new FakeImporterSource(A, B)
            .add(A, 100)
            .add(B, 100);
        final ImporterTransferStrategy strategy = new ImporterTransferStrategyImpl(source, 1);
        sut.setTransferStrategies(List.of(strategy));

        // Act
        sut.doWork();

        // Assert
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(C, 100)
        );
        assertThat(source.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 100),
            new ResourceAmount(B, 100)
        );
    }

    @Test
    void testTransferDifferentResourceOverMultipleSlots(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage
    ) {
        // Arrange
        storage.addSource(new InMemoryStorageImpl());

        final FakeImporterSource source = new FakeImporterSource(A, B, A, B)
            .add(A, 11)
            .add(B, 6);
        final ImporterTransferStrategy strategy = new ImporterTransferStrategyImpl(source, 10);
        sut.setTransferStrategies(List.of(strategy));

        // Act
        sut.doWork();

        // Assert
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 10)
        );
        assertThat(source.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 1),
            new ResourceAmount(B, 6)
        );
    }

    @Test
    void testTransferSameResourceOverMultipleSlots(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage
    ) {
        // Arrange
        storage.addSource(new InMemoryStorageImpl());

        final FakeImporterSource source = new FakeImporterSource(A, A, A, B)
            .add(A, 20)
            .add(B, 5);

        final ImporterTransferStrategy strategy = new ImporterTransferStrategyImpl(source, 10);
        sut.setTransferStrategies(List.of(strategy));

        // Act
        sut.doWork();

        // Assert
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 10)
        );
        assertThat(source.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 10),
            new ResourceAmount(B, 5)
        );
    }

    @Test
    void testTransferWhereResourceIsNotAccepted(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage
    ) {
        // Arrange
        storage.addSource(new InMemoryStorageImpl() {
            @Override
            public long insert(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
                if (A.equals(resource)) {
                    return 0;
                }
                return super.insert(resource, amount, action, actor);
            }
        });

        final FakeImporterSource source = new FakeImporterSource(A, B, B, B)
            .add(A, 8)
            .add(B, 11);
        final ImporterTransferStrategy strategy = new ImporterTransferStrategyImpl(source, 10);
        sut.setTransferStrategies(List.of(strategy));

        // Act
        sut.doWork();

        // Assert
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(B, 10)
        );
        assertThat(source.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 8),
            new ResourceAmount(B, 1)
        );
    }

    @Test
    void testTransferWithoutAnyResourcesInSource(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage
    ) {
        // Arrange
        storage.addSource(new InMemoryStorageImpl());

        final FakeImporterSource source = new FakeImporterSource();
        final ImporterTransferStrategy strategy = new ImporterTransferStrategyImpl(source, 10);
        sut.setTransferStrategies(List.of(strategy));

        // Act
        sut.doWork();

        // Assert
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().isEmpty();
        assertThat(source.getAll()).isEmpty();
    }

    @Test
    void shouldRespectAllowlist(@InjectNetworkStorageComponent final StorageNetworkComponent storage) {
        // Arrange
        sut.setFilterMode(FilterMode.ALLOW);
        sut.setFilters(Set.of(A));

        storage.addSource(new InMemoryStorageImpl());

        final FakeImporterSource source = new FakeImporterSource(B, A)
            .add(B, 10)
            .add(A, 10);

        final ImporterTransferStrategy strategy = new ImporterTransferStrategyImpl(source, 1);
        sut.setTransferStrategies(List.of(strategy));

        // Act
        sut.doWork();

        // Assert
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 1)
        );
        assertThat(source.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(B, 10),
            new ResourceAmount(A, 9)
        );
    }

    @Test
    void shouldRespectAllowlistWithNormalizer(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage
    ) {
        // Arrange
        sut.setFilterMode(FilterMode.ALLOW);
        sut.setFilters(Set.of(A));
        sut.setNormalizer(resource -> {
            if (resource == A_ALTERNATIVE || resource == A_ALTERNATIVE2) {
                return A;
            }
            return resource;
        });

        storage.addSource(new InMemoryStorageImpl());

        final FakeImporterSource source = new FakeImporterSource(B, A_ALTERNATIVE, A_ALTERNATIVE2)
            .add(B, 10)
            .add(A_ALTERNATIVE, 1)
            .add(A_ALTERNATIVE2, 1);

        final ImporterTransferStrategy strategy = new ImporterTransferStrategyImpl(source, 10);
        sut.setTransferStrategies(List.of(strategy));

        // Act
        sut.doWork();
        sut.doWork();

        // Assert
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A_ALTERNATIVE, 1),
            new ResourceAmount(A_ALTERNATIVE2, 1)
        );
        assertThat(source.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(B, 10)
        );
    }

    @Test
    void shouldRespectAllowlistWithoutAlternative(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage
    ) {
        // Arrange
        sut.setFilterMode(FilterMode.ALLOW);
        sut.setFilters(Set.of(A));

        storage.addSource(new InMemoryStorageImpl());

        final FakeImporterSource source = new FakeImporterSource(B)
            .add(B, 10);

        final ImporterTransferStrategy strategy = new ImporterTransferStrategyImpl(source, 1);
        sut.setTransferStrategies(List.of(strategy));

        // Act
        sut.doWork();

        // Assert
        assertThat(storage.getAll()).isEmpty();
        assertThat(source.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(B, 10)
        );
    }

    @Test
    void shouldRespectEmptyAllowlist(@InjectNetworkStorageComponent final StorageNetworkComponent storage) {
        // Arrange
        sut.setFilterMode(FilterMode.ALLOW);
        sut.setFilters(Set.of());

        storage.addSource(new InMemoryStorageImpl());

        final FakeImporterSource source = new FakeImporterSource(B, A)
            .add(B, 10)
            .add(A, 10);

        final ImporterTransferStrategy strategy = new ImporterTransferStrategyImpl(source, 1);
        sut.setTransferStrategies(List.of(strategy));

        // Act
        sut.doWork();

        // Assert
        assertThat(storage.getAll()).isEmpty();
        assertThat(source.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(B, 10),
            new ResourceAmount(A, 10)
        );
    }

    @Test
    void shouldRespectBlocklist(@InjectNetworkStorageComponent final StorageNetworkComponent storage) {
        // Arrange
        sut.setFilterMode(FilterMode.BLOCK);
        sut.setFilters(Set.of(A));

        storage.addSource(new InMemoryStorageImpl());

        final FakeImporterSource source = new FakeImporterSource(A, B)
            .add(A, 10)
            .add(B, 10);

        final ImporterTransferStrategy strategy = new ImporterTransferStrategyImpl(source, 1);
        sut.setTransferStrategies(List.of(strategy));

        // Act
        sut.doWork();

        // Assert
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(B, 1)
        );
        assertThat(source.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 10),
            new ResourceAmount(B, 9)
        );
    }

    @Test
    void shouldRespectBlocklistWithoutAlternative(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage
    ) {
        // Arrange
        sut.setFilterMode(FilterMode.BLOCK);
        sut.setFilters(Set.of(A));

        storage.addSource(new InMemoryStorageImpl());

        final FakeImporterSource source = new FakeImporterSource(A)
            .add(A, 10);

        final ImporterTransferStrategy strategy = new ImporterTransferStrategyImpl(source, 1);
        sut.setTransferStrategies(List.of(strategy));

        // Act
        sut.doWork();

        // Assert
        assertThat(storage.getAll()).isEmpty();
        assertThat(source.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 10)
        );
    }

    @Test
    void shouldRespectEmptyBlocklist(@InjectNetworkStorageComponent final StorageNetworkComponent storage) {
        // Arrange
        sut.setFilterMode(FilterMode.BLOCK);
        sut.setFilters(Set.of());

        storage.addSource(new InMemoryStorageImpl());

        final FakeImporterSource source = new FakeImporterSource(A, B)
            .add(A, 10)
            .add(B, 10);

        final ImporterTransferStrategy strategy = new ImporterTransferStrategyImpl(source, 1);
        sut.setTransferStrategies(List.of(strategy));

        // Act
        sut.doWork();

        // Assert
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 1)
        );
        assertThat(source.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 9),
            new ResourceAmount(B, 10)
        );
    }
}
