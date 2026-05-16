package com.refinedmods.refinedstorage.api.network.impl.node.relay;

import com.refinedmods.refinedstorage.api.autocrafting.task.ExternalPatternSinkKey;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.energy.EnergyNetworkComponent;
import com.refinedmods.refinedstorage.api.network.security.SecurityNetworkComponent;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.filter.FilterMode;
import com.refinedmods.refinedstorage.api.storage.AccessMode;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.api.storage.StorageImpl;
import com.refinedmods.refinedstorage.api.storage.composite.PriorityStorage;
import com.refinedmods.refinedstorage.api.storage.limited.LimitedStorageImpl;
import com.refinedmods.refinedstorage.network.test.AddNetworkNode;
import com.refinedmods.refinedstorage.network.test.InjectNetwork;
import com.refinedmods.refinedstorage.network.test.InjectNetworkEnergyComponent;
import com.refinedmods.refinedstorage.network.test.InjectNetworkSecurityComponent;
import com.refinedmods.refinedstorage.network.test.InjectNetworkStorageComponent;
import com.refinedmods.refinedstorage.network.test.NetworkTest;
import com.refinedmods.refinedstorage.network.test.SetupNetwork;
import com.refinedmods.refinedstorage.network.test.fixtures.PermissionFixtures;
import com.refinedmods.refinedstorage.network.test.fixtures.SecurityActorFixtures;

import java.util.Set;

import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage.api.network.impl.node.relay.RelayNetworkNodeTest.addSecurityPolicy;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.A;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.A_ALTERNATIVE;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.B;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.C;
import static com.refinedmods.refinedstorage.network.test.nodefactory.AbstractNetworkNodeFactory.PROPERTY_ACTIVE;
import static org.assertj.core.api.Assertions.assertThat;

@NetworkTest
@SetupNetwork(id = "input")
@SetupNetwork(id = "output", setupEnergy = false)
class RelayStorageNetworkNodeTest {
    @SuppressWarnings("DefaultAnnotationParam")
    @AddNetworkNode(properties = {
        @AddNetworkNode.Property(key = PROPERTY_ACTIVE, boolValue = false)
    }, networkId = "input")
    private RelayInputNetworkNode input;

    @AddNetworkNode(networkId = "output")
    private RelayOutputNetworkNode output;

    @Test
    void shouldPassStorageComponent(
        @InjectNetworkSecurityComponent(networkId = "input") final SecurityNetworkComponent inputSecurity,
        @InjectNetworkStorageComponent(networkId = "input") final StorageNetworkComponent inputStorage,
        @InjectNetworkEnergyComponent(networkId = "output") final EnergyNetworkComponent outputEnergy,
        @InjectNetworkSecurityComponent(networkId = "output") final SecurityNetworkComponent outputSecurity,
        @InjectNetworkStorageComponent(networkId = "output") final StorageNetworkComponent outputStorage
    ) {
        // Arrange
        input.setActive(true);
        input.setOutputNode(output);

        addSecurityPolicy(inputSecurity, PermissionFixtures.OTHER);

        inputStorage.addSource(new StorageImpl());
        inputStorage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        // Act
        input.setComponentTypes(Set.of(RelayComponentType.STORAGE));

        inputStorage.insert(B, 5, Action.EXECUTE, Actor.EMPTY);
        inputStorage.extract(B, 3, Action.EXECUTE, Actor.EMPTY);

        // Assert
        assertThat(outputEnergy.getCapacity()).isZero();
        assertThat(outputEnergy.getStored()).isZero();
        assertThat(outputSecurity.isAllowed(PermissionFixtures.ALLOW_BY_DEFAULT, SecurityActorFixtures.A)).isTrue();
        assertThat(outputSecurity.isAllowed(PermissionFixtures.OTHER, SecurityActorFixtures.A)).isFalse();

        assertThat(inputStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 10),
            new ResourceAmount(B, 2)
        );
        assertThat(outputStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 10),
            new ResourceAmount(B, 2)
        );
        assertThat(outputStorage.getStored()).isEqualTo(12);
        assertThat(input.hasComponentType(RelayComponentType.ENERGY)).isFalse();
        assertThat(input.hasComponentType(RelayComponentType.SECURITY)).isFalse();
        assertThat(input.hasComponentType(RelayComponentType.STORAGE)).isTrue();
    }

    @Test
    void shouldRemoveStorageWhenNetworkIsRemoved(
        @InjectNetworkStorageComponent(networkId = "input") final StorageNetworkComponent inputStorage,
        @InjectNetworkStorageComponent(networkId = "output") final StorageNetworkComponent outputStorage
    ) {
        // Arrange
        input.setActive(true);
        input.setOutputNode(output);

        inputStorage.addSource(new StorageImpl());
        inputStorage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);
        input.setComponentTypes(Set.of(RelayComponentType.STORAGE));
        inputStorage.insert(B, 5, Action.EXECUTE, Actor.EMPTY);
        inputStorage.extract(B, 3, Action.EXECUTE, Actor.EMPTY);

        // Act
        input.setNetwork(null);

        inputStorage.insert(C, 1, Action.EXECUTE, Actor.EMPTY);

        // Assert
        assertThat(inputStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 10),
            new ResourceAmount(B, 2),
            new ResourceAmount(C, 1)
        );
        assertThat(outputStorage.getAll()).isEmpty();
        assertThat(outputStorage.getStored()).isZero();
        assertThat(outputStorage.insert(A, 1, Action.EXECUTE, Actor.EMPTY)).isZero();
        assertThat(outputStorage.extract(A, 1, Action.EXECUTE, Actor.EMPTY)).isZero();
    }

    @Test
    @SetupNetwork(id = "output_alt")
    void shouldNotNotifyOldOutputNetworkWhenOutputNetworkHasChanged(
        @InjectNetworkStorageComponent(networkId = "input") final StorageNetworkComponent inputStorage,
        @InjectNetwork("output") final Network outputNetwork,
        @InjectNetworkStorageComponent(networkId = "output_alt") final StorageNetworkComponent outputAlternativeStorage,
        @InjectNetwork("output_alt") final Network outputAlternativeNetwork,
        @InjectNetworkStorageComponent(networkId = "output") final StorageNetworkComponent outputStorage
    ) {
        // Arrange
        input.setActive(true);
        input.setOutputNode(output);

        inputStorage.addSource(new StorageImpl());
        inputStorage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);
        input.setComponentTypes(Set.of(RelayComponentType.STORAGE));

        // Act
        outputNetwork.removeContainer(() -> output);
        outputAlternativeNetwork.addContainer(() -> output);
        output.setNetwork(outputAlternativeNetwork);

        inputStorage.insert(A, 1, Action.EXECUTE, Actor.EMPTY);

        // Assert
        assertThat(outputAlternativeStorage.getAll()).usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(new ResourceAmount(A, 11));
        assertThat(outputAlternativeStorage.getStored()).isEqualTo(11);
        assertThat(outputStorage.getAll()).isEmpty();
        assertThat(outputStorage.getStored()).isZero();
    }

    @Test
    void shouldInsertResourcesIntoInputStorageFromOutputStorage(
        @InjectNetworkStorageComponent(networkId = "input") final StorageNetworkComponent inputStorage,
        @InjectNetworkStorageComponent(networkId = "output") final StorageNetworkComponent outputStorage
    ) {
        // Arrange
        input.setActive(true);
        input.setOutputNode(output);
        input.setComponentTypes(Set.of(RelayComponentType.STORAGE));

        inputStorage.addSource(new StorageImpl());
        inputStorage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        // Act
        final long inserted = outputStorage.insert(B, 2, Action.EXECUTE, Actor.EMPTY);

        // Assert
        assertThat(inserted).isEqualTo(2);
        assertThat(inputStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 10),
            new ResourceAmount(B, 2)
        );
        assertThat(inputStorage.getStored()).isEqualTo(12);
        assertThat(outputStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 10),
            new ResourceAmount(B, 2)
        );
        assertThat(outputStorage.getStored()).isEqualTo(12);
    }

    @Test
    void shouldExtractResourcesFromInputStorageIntoOutputStorage(
        @InjectNetworkStorageComponent(networkId = "input") final StorageNetworkComponent inputStorage,
        @InjectNetworkStorageComponent(networkId = "output") final StorageNetworkComponent outputStorage
    ) {
        // Arrange
        input.setActive(true);
        input.setOutputNode(output);
        input.setComponentTypes(Set.of(RelayComponentType.STORAGE));

        inputStorage.addSource(new StorageImpl());
        inputStorage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        // Act
        final long extracted = outputStorage.extract(A, 2, Action.EXECUTE, Actor.EMPTY);

        // Assert
        assertThat(extracted).isEqualTo(2);
        assertThat(inputStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 8)
        );
        assertThat(inputStorage.getStored()).isEqualTo(8);
        assertThat(outputStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 8)
        );
        assertThat(outputStorage.getStored()).isEqualTo(8);
    }

    @Test
    void shouldNotInsertInExtractOnlyMode(
        @InjectNetworkStorageComponent(networkId = "input") final StorageNetworkComponent inputStorage,
        @InjectNetworkStorageComponent(networkId = "output") final StorageNetworkComponent outputStorage
    ) {
        // Arrange
        input.setActive(true);
        input.setOutputNode(output);
        input.setAccessMode(AccessMode.EXTRACT);
        input.setComponentTypes(Set.of(RelayComponentType.STORAGE));

        inputStorage.addSource(new StorageImpl());
        inputStorage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        // Act
        final long inserted = outputStorage.insert(B, 2, Action.EXECUTE, Actor.EMPTY);
        final long extracted = outputStorage.extract(A, 2, Action.EXECUTE, Actor.EMPTY);

        // Assert
        assertThat(inserted).isZero();
        assertThat(extracted).isEqualTo(2);
        assertThat(inputStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 8)
        );
        assertThat(inputStorage.getStored()).isEqualTo(8);
        assertThat(outputStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 8)
        );
        assertThat(outputStorage.getStored()).isEqualTo(8);
    }

    @Test
    void shouldNotExtractInInsertOnlyMode(
        @InjectNetworkStorageComponent(networkId = "input") final StorageNetworkComponent inputStorage,
        @InjectNetworkStorageComponent(networkId = "output") final StorageNetworkComponent outputStorage
    ) {
        // Arrange
        input.setActive(true);
        input.setOutputNode(output);
        input.setAccessMode(AccessMode.INSERT);
        input.setComponentTypes(Set.of(RelayComponentType.STORAGE));

        inputStorage.addSource(new StorageImpl());
        inputStorage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        // Act
        final long inserted = outputStorage.insert(B, 2, Action.EXECUTE, Actor.EMPTY);
        final long extracted = outputStorage.extract(A, 2, Action.EXECUTE, Actor.EMPTY);

        // Assert
        assertThat(inserted).isEqualTo(2);
        assertThat(extracted).isZero();
        assertThat(inputStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 10),
            new ResourceAmount(B, 2)
        );
        assertThat(inputStorage.getStored()).isEqualTo(12);
        assertThat(outputStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 10),
            new ResourceAmount(B, 2)
        );
        assertThat(outputStorage.getStored()).isEqualTo(12);
    }

    @Test
    void shouldRespectInsertPriorityOfOutput(
        @InjectNetworkStorageComponent(networkId = "input") final StorageNetworkComponent inputStorage,
        @InjectNetworkStorageComponent(networkId = "output") final StorageNetworkComponent outputStorage
    ) {
        // Arrange
        input.setActive(true);
        input.setOutputNode(output);
        input.setInsertPriority(3);
        input.setComponentTypes(Set.of(RelayComponentType.STORAGE));
        inputStorage.addSource(new LimitedStorageImpl(10));

        final Storage fallbackStorage1 = PriorityStorage.of(new LimitedStorageImpl(10), 2, 1);
        outputStorage.addSource(fallbackStorage1);

        final Storage fallbackStorage2 = PriorityStorage.of(new LimitedStorageImpl(10), 1, 2);
        outputStorage.addSource(fallbackStorage2);

        // Act
        final long inserted = outputStorage.insert(A, 12, Action.EXECUTE, Actor.EMPTY);

        // Assert
        assertThat(inserted).isEqualTo(12);

        assertThat(inputStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 10)
        );
        assertThat(fallbackStorage1.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 2)
        );
        assertThat(fallbackStorage2.getAll()).usingRecursiveFieldByFieldElementComparator().isEmpty();
    }

    @Test
    void shouldRespectExtractPriorityOfOutput(
        @InjectNetworkStorageComponent(networkId = "input") final StorageNetworkComponent inputStorage,
        @InjectNetworkStorageComponent(networkId = "output") final StorageNetworkComponent outputStorage
    ) {
        // Arrange
        input.setActive(true);
        input.setOutputNode(output);
        input.setExtractPriority(3);
        input.setComponentTypes(Set.of(RelayComponentType.STORAGE));
        inputStorage.addSource(new StorageImpl());
        inputStorage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        final Storage fallbackStorage1 = PriorityStorage.of(new StorageImpl(), 2, 1);
        fallbackStorage1.insert(A, 10, Action.EXECUTE, Actor.EMPTY);
        outputStorage.addSource(fallbackStorage1);

        final Storage fallbackStorage2 = PriorityStorage.of(new StorageImpl(), 1, 2);
        fallbackStorage2.insert(A, 10, Action.EXECUTE, Actor.EMPTY);
        outputStorage.addSource(fallbackStorage2);

        // Act
        final long extracted = outputStorage.extract(A, 3, Action.EXECUTE, Actor.EMPTY);

        // Assert
        assertThat(extracted).isEqualTo(3);

        assertThat(inputStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 7)
        );
        assertThat(fallbackStorage1.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 10)
        );
        assertThat(fallbackStorage2.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 10)
        );
    }

    @Test
    void shouldModifyInsertPriorityOfOutput(
        @InjectNetworkStorageComponent(networkId = "input") final StorageNetworkComponent inputStorage,
        @InjectNetworkStorageComponent(networkId = "output") final StorageNetworkComponent outputStorage
    ) {
        // Arrange
        input.setActive(true);
        input.setOutputNode(output);
        input.setInsertPriority(3);
        input.setComponentTypes(Set.of(RelayComponentType.STORAGE));
        inputStorage.addSource(new LimitedStorageImpl(10));

        final Storage fallbackStorage1 = PriorityStorage.of(new LimitedStorageImpl(5), 1, 3);
        outputStorage.addSource(fallbackStorage1);

        final Storage fallbackStorage2 = PriorityStorage.of(new LimitedStorageImpl(5), 3, 1);
        outputStorage.addSource(fallbackStorage2);

        // Act
        input.setInsertPriority(2);

        // Assert
        final long inserted = outputStorage.insert(A, 7, Action.EXECUTE, Actor.EMPTY);
        final long extracted = outputStorage.extract(A, 3, Action.EXECUTE, Actor.EMPTY);

        assertThat(inserted).isEqualTo(7);
        assertThat(extracted).isEqualTo(3);

        assertThat(inputStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 2)
        );
        assertThat(fallbackStorage1.getAll()).usingRecursiveFieldByFieldElementComparator().isEmpty();
        assertThat(fallbackStorage2.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 2)
        );
    }

    @Test
    void shouldModifyExtractPriorityOfOutput(
        @InjectNetworkStorageComponent(networkId = "input") final StorageNetworkComponent inputStorage,
        @InjectNetworkStorageComponent(networkId = "output") final StorageNetworkComponent outputStorage
    ) {
        // Arrange
        input.setActive(true);
        input.setOutputNode(output);
        input.setExtractPriority(4);
        input.setComponentTypes(Set.of(RelayComponentType.STORAGE));
        inputStorage.addSource(new LimitedStorageImpl(10));
        inputStorage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        final Storage fallbackStorage1 = PriorityStorage.of(new LimitedStorageImpl(5), 1, 3);
        fallbackStorage1.insert(A, 5, Action.EXECUTE, Actor.EMPTY);
        outputStorage.addSource(fallbackStorage1);

        final Storage fallbackStorage2 = PriorityStorage.of(new LimitedStorageImpl(5), 3, 1);
        fallbackStorage2.insert(A, 5, Action.EXECUTE, Actor.EMPTY);
        outputStorage.addSource(fallbackStorage2);

        // Act
        input.setExtractPriority(2);
        final long extracted = outputStorage.extract(A, 6, Action.EXECUTE, Actor.EMPTY);

        // Assert
        assertThat(extracted).isEqualTo(6);

        assertThat(inputStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 9)
        );
        assertThat(fallbackStorage1.getAll()).usingRecursiveFieldByFieldElementComparator().isEmpty();
        assertThat(fallbackStorage2.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 5)
        );
    }

    @Test
    void shouldRespectBlocklistFilter(
        @InjectNetworkStorageComponent(networkId = "input") final StorageNetworkComponent inputStorage,
        @InjectNetworkStorageComponent(networkId = "output") final StorageNetworkComponent outputStorage
    ) {
        // Arrange
        input.setActive(true);
        input.setOutputNode(output);
        input.setFilters(Set.of(A, B));

        inputStorage.addSource(new StorageImpl());
        inputStorage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);
        inputStorage.insert(B, 9, Action.EXECUTE, Actor.EMPTY);
        inputStorage.insert(C, 8, Action.EXECUTE, Actor.EMPTY);

        // Act
        input.setComponentTypes(Set.of(RelayComponentType.STORAGE));

        // This update shouldn't arrive.
        inputStorage.insert(A, 1, Action.EXECUTE, Actor.EMPTY);
        // This one should.
        inputStorage.insert(C, 3, Action.EXECUTE, Actor.EMPTY);

        final long insertedAllowed = outputStorage.insert(C, 2, Action.EXECUTE, Actor.EMPTY);
        final long insertedNotAllowed = outputStorage.insert(A, 1, Action.EXECUTE, Actor.EMPTY);

        final long extractedAllowed = outputStorage.extract(C, 1, Action.EXECUTE, Actor.EMPTY);
        final long extractedNotAllowed = outputStorage.extract(A, 1, Action.EXECUTE, Actor.EMPTY);

        // Assert
        assertThat(insertedAllowed).isEqualTo(2);
        assertThat(insertedNotAllowed).isZero();

        assertThat(extractedAllowed).isEqualTo(1);
        assertThat(extractedNotAllowed).isZero();

        assertThat(inputStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 11),
            new ResourceAmount(B, 9),
            new ResourceAmount(C, 12)
        );
        assertThat(inputStorage.getStored()).isEqualTo(11 + 9 + 12);

        assertThat(outputStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(C, 12)
        );
        assertThat(outputStorage.getStored()).isEqualTo(12);
    }

    @Test
    void shouldRespectAllowlistFilter(
        @InjectNetworkStorageComponent(networkId = "input") final StorageNetworkComponent inputStorage,
        @InjectNetworkStorageComponent(networkId = "output") final StorageNetworkComponent outputStorage
    ) {
        // Arrange
        input.setActive(true);
        input.setOutputNode(output);
        input.setFilters(Set.of(A, B));
        input.setFilterMode(FilterMode.ALLOW);

        inputStorage.addSource(new StorageImpl());
        inputStorage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);
        inputStorage.insert(B, 9, Action.EXECUTE, Actor.EMPTY);
        inputStorage.insert(C, 8, Action.EXECUTE, Actor.EMPTY);

        // Act
        input.setComponentTypes(Set.of(RelayComponentType.STORAGE));

        // This update should arrive.
        inputStorage.insert(A, 1, Action.EXECUTE, Actor.EMPTY);
        // This one shouldn't.
        inputStorage.insert(C, 3, Action.EXECUTE, Actor.EMPTY);

        final long insertedAllowed = outputStorage.insert(A, 2, Action.EXECUTE, Actor.EMPTY);
        final long insertedNotAllowed = outputStorage.insert(C, 1, Action.EXECUTE, Actor.EMPTY);

        final long extractedAllowed = outputStorage.extract(A, 1, Action.EXECUTE, Actor.EMPTY);
        final long extractedNotAllowed = outputStorage.extract(C, 1, Action.EXECUTE, Actor.EMPTY);

        // Assert
        assertThat(insertedAllowed).isEqualTo(2);
        assertThat(insertedNotAllowed).isZero();

        assertThat(extractedAllowed).isEqualTo(1);
        assertThat(extractedNotAllowed).isZero();

        assertThat(inputStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 12),
            new ResourceAmount(B, 9),
            new ResourceAmount(C, 11)
        );
        assertThat(inputStorage.getStored()).isEqualTo(12 + 9 + 11);

        assertThat(outputStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 12),
            new ResourceAmount(B, 9)
        );
        assertThat(outputStorage.getStored()).isEqualTo(12 + 9);
    }

    @Test
    void shouldRespectFilterNormalizer(
        @InjectNetworkStorageComponent(networkId = "input") final StorageNetworkComponent inputStorage,
        @InjectNetworkStorageComponent(networkId = "output") final StorageNetworkComponent outputStorage
    ) {
        // Arrange
        input.setActive(true);
        input.setOutputNode(output);
        input.setFilters(Set.of(A, B));
        input.setFilterMode(FilterMode.ALLOW);
        input.setFilterNormalizer(resource -> {
            if (resource == A_ALTERNATIVE) {
                return A;
            }
            return resource;
        });

        inputStorage.addSource(new StorageImpl());
        inputStorage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);
        inputStorage.insert(A_ALTERNATIVE, 3, Action.EXECUTE, Actor.EMPTY);
        inputStorage.insert(B, 9, Action.EXECUTE, Actor.EMPTY);
        inputStorage.insert(C, 8, Action.EXECUTE, Actor.EMPTY);

        // Act
        input.setComponentTypes(Set.of(RelayComponentType.STORAGE));

        // These updates should arrive.
        inputStorage.insert(A, 1, Action.EXECUTE, Actor.EMPTY);
        inputStorage.insert(A_ALTERNATIVE, 2, Action.EXECUTE, Actor.EMPTY);
        // This one shouldn't.
        inputStorage.insert(C, 3, Action.EXECUTE, Actor.EMPTY);

        final long insertedAllowed1 = outputStorage.insert(A, 2, Action.EXECUTE, Actor.EMPTY);
        final long insertedAllowed2 = outputStorage.insert(A_ALTERNATIVE, 3, Action.EXECUTE, Actor.EMPTY);
        final long insertedNotAllowed = outputStorage.insert(C, 1, Action.EXECUTE, Actor.EMPTY);

        final long extractedAllowed = outputStorage.extract(A, 1, Action.EXECUTE, Actor.EMPTY);
        final long extractedAllowed2 = outputStorage.extract(A_ALTERNATIVE, 2, Action.EXECUTE, Actor.EMPTY);
        final long extractedNotAllowed = outputStorage.extract(C, 1, Action.EXECUTE, Actor.EMPTY);

        // Assert
        assertThat(insertedAllowed1).isEqualTo(2);
        assertThat(insertedAllowed2).isEqualTo(3);
        assertThat(insertedNotAllowed).isZero();

        assertThat(extractedAllowed).isEqualTo(1);
        assertThat(extractedAllowed2).isEqualTo(2);
        assertThat(extractedNotAllowed).isZero();

        assertThat(inputStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 12),
            new ResourceAmount(A_ALTERNATIVE, 6),
            new ResourceAmount(B, 9),
            new ResourceAmount(C, 11)
        );
        assertThat(inputStorage.getStored()).isEqualTo(12 + 6 + 9 + 11);

        assertThat(outputStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 12),
            new ResourceAmount(A_ALTERNATIVE, 6),
            new ResourceAmount(B, 9)
        );
        assertThat(outputStorage.getStored()).isEqualTo(12 + 6 + 9);
    }

    @Test
    void shouldUpdateOutputStorageWhenFiltersAreChanged(
        @InjectNetworkStorageComponent(networkId = "input") final StorageNetworkComponent inputStorage,
        @InjectNetworkStorageComponent(networkId = "output") final StorageNetworkComponent outputStorage
    ) {
        // Arrange
        input.setActive(true);
        input.setOutputNode(output);
        input.setFilters(Set.of(A));
        input.setFilterMode(FilterMode.BLOCK);

        inputStorage.addSource(new StorageImpl());
        inputStorage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);
        inputStorage.insert(B, 9, Action.EXECUTE, Actor.EMPTY);
        inputStorage.insert(C, 8, Action.EXECUTE, Actor.EMPTY);

        // Act
        input.setComponentTypes(Set.of(RelayComponentType.STORAGE));
        input.setFilters(Set.of(B));

        // Assert
        assertThat(inputStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 10),
            new ResourceAmount(B, 9),
            new ResourceAmount(C, 8)
        );
        assertThat(inputStorage.getStored()).isEqualTo(10 + 9 + 8);

        assertThat(outputStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 10),
            new ResourceAmount(C, 8)
        );
        assertThat(outputStorage.getStored()).isEqualTo(10 + 8);
    }

    @Test
    void shouldUpdateOutputStorageWhenFilterModeIsChanged(
        @InjectNetworkStorageComponent(networkId = "input") final StorageNetworkComponent inputStorage,
        @InjectNetworkStorageComponent(networkId = "output") final StorageNetworkComponent outputStorage
    ) {
        // Arrange
        input.setActive(true);
        input.setOutputNode(output);
        input.setFilters(Set.of(A));
        input.setFilterMode(FilterMode.BLOCK);

        inputStorage.addSource(new StorageImpl());
        inputStorage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);
        inputStorage.insert(B, 9, Action.EXECUTE, Actor.EMPTY);
        inputStorage.insert(C, 8, Action.EXECUTE, Actor.EMPTY);

        // Act
        input.setComponentTypes(Set.of(RelayComponentType.STORAGE));
        input.setFilterMode(FilterMode.ALLOW);

        // Assert
        assertThat(inputStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 10),
            new ResourceAmount(B, 9),
            new ResourceAmount(C, 8)
        );
        assertThat(inputStorage.getStored()).isEqualTo(10 + 9 + 8);

        assertThat(outputStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 10)
        );
        assertThat(outputStorage.getStored()).isEqualTo(10);
    }

    @Test
    @SetupNetwork(id = "cycle_input", energyStored = 1, energyCapacity = 2)
    @SetupNetwork(id = "cycle_input_alt", energyStored = 3, energyCapacity = 4)
    void shouldDetectCycles(
        @InjectNetwork("cycle_input") final Network inputNetwork,
        @InjectNetworkStorageComponent(networkId = "cycle_input") final StorageNetworkComponent inputStorage,
        @InjectNetwork("cycle_input_alt") final Network inputAlternativeNetwork,
        @InjectNetworkStorageComponent(networkId = "cycle_input_alt")
        final StorageNetworkComponent inputAlternativeStorage
    ) {
        // Act
        final RelayOutputNetworkNode cycleOutput = new RelayOutputNetworkNode(0);
        final ExternalPatternSinkKey cycleOutputKey = new ExternalPatternSinkKey() {
        };
        cycleOutput.setSinkKeyProvider(() -> cycleOutputKey);
        cycleOutput.setStorageDelegate(inputAlternativeStorage);
        cycleOutput.setNetwork(inputNetwork);
        inputNetwork.addContainer(() -> cycleOutput);

        final RelayOutputNetworkNode cycleOutputAlternative = new RelayOutputNetworkNode(0);
        final ExternalPatternSinkKey cycleOutputAlternativeKey = new ExternalPatternSinkKey() {
        };
        cycleOutputAlternative.setSinkKeyProvider(() -> cycleOutputAlternativeKey);
        cycleOutputAlternative.setStorageDelegate(inputStorage);
        cycleOutputAlternative.setNetwork(inputAlternativeNetwork);
        inputAlternativeNetwork.addContainer(() -> cycleOutputAlternative);

        inputStorage.addSource(new StorageImpl());

        // Assert
        final long inserted1 = inputStorage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);
        final long inserted2 = inputAlternativeStorage.insert(A, 2, Action.EXECUTE, Actor.EMPTY);

        final long extracted1 = inputStorage.extract(A, 1, Action.EXECUTE, Actor.EMPTY);
        final long extracted2 = inputAlternativeStorage.extract(A, 1, Action.EXECUTE, Actor.EMPTY);

        assertThat(inserted1).isEqualTo(10);
        assertThat(inserted2).isZero();

        assertThat(extracted1).isEqualTo(1);
        assertThat(extracted2).isZero();

        assertThat(inputStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 9)
        );
        assertThat(inputAlternativeStorage.getAll()).isEmpty();

        assertThat(inputStorage.getStored()).isEqualTo(9);
        assertThat(inputAlternativeStorage.getStored()).isZero();
    }
}
