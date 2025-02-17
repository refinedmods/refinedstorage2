package com.refinedmods.refinedstorage.api.network.impl.node.iface;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.StorageImpl;
import com.refinedmods.refinedstorage.api.storage.limited.LimitedStorageImpl;
import com.refinedmods.refinedstorage.network.test.AddNetworkNode;
import com.refinedmods.refinedstorage.network.test.InjectNetworkStorageComponent;
import com.refinedmods.refinedstorage.network.test.NetworkTest;
import com.refinedmods.refinedstorage.network.test.SetupNetwork;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.A;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.A_ALTERNATIVE;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.A_ALTERNATIVE2;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.B;
import static org.assertj.core.api.Assertions.assertThat;

@NetworkTest
@SetupNetwork
class KeepExportingInterfaceNetworkNodeTest {
    @AddNetworkNode
    InterfaceNetworkNode sut;

    private InterfaceExportStateImpl exportState;

    @BeforeEach
    void setUp() {
        exportState = new InterfaceExportStateImpl(2);
        sut.setExportState(exportState);
        sut.setTransferQuotaProvider(resource -> 2);
        sut.setEnergyUsage(5);
    }

    @Test
    void shouldKeepExportingResourceUntilWantedAmountIsReached(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage
    ) {
        // Arrange
        storage.addSource(new StorageImpl());
        storage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        exportState.setRequestedResource(1, A, 7);

        // Act & assert
        sut.doWork();
        assertThat(exportState.getExportedResource(0)).isNull();
        assertThat(exportState.getExportedResource(1)).isEqualTo(A);
        assertThat(exportState.getExportedAmount(1)).isEqualTo(2);
        assertThat(storage.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new ResourceAmount(A, 8));

        sut.doWork();
        assertThat(exportState.getExportedResource(0)).isNull();
        assertThat(exportState.getExportedResource(1)).isEqualTo(A);
        assertThat(exportState.getExportedAmount(1)).isEqualTo(4);
        assertThat(storage.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new ResourceAmount(A, 6));

        sut.doWork();
        assertThat(exportState.getExportedResource(0)).isNull();
        assertThat(exportState.getExportedResource(1)).isEqualTo(A);
        assertThat(exportState.getExportedAmount(1)).isEqualTo(6);
        assertThat(storage.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new ResourceAmount(A, 4));

        sut.doWork();
        assertThat(exportState.getExportedResource(0)).isNull();
        assertThat(exportState.getExportedResource(1)).isEqualTo(A);
        assertThat(exportState.getExportedAmount(1)).isEqualTo(7);
        assertThat(storage.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new ResourceAmount(A, 3));

        sut.doWork();
        sut.doWork();
        sut.doWork();
        assertThat(exportState.getExportedResource(0)).isNull();
        assertThat(exportState.getExportedResource(1)).isEqualTo(A);
        assertThat(exportState.getExportedAmount(1)).isEqualTo(7);
        assertThat(storage.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new ResourceAmount(A, 3));
    }

    @Test
    void shouldKeepExportingResourceUntilWantedAmountIsReachedAndNetworkHasEnoughResources(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage
    ) {
        // Arrange
        storage.addSource(new StorageImpl());
        storage.insert(A, 7, Action.EXECUTE, Actor.EMPTY);

        exportState.setRequestedResource(1, A, 10);

        // Act & assert
        sut.doWork();
        assertThat(exportState.getExportedResource(0)).isNull();
        assertThat(exportState.getExportedResource(1)).isEqualTo(A);
        assertThat(exportState.getExportedAmount(1)).isEqualTo(2);
        assertThat(storage.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new ResourceAmount(A, 5));

        sut.doWork();
        assertThat(exportState.getExportedResource(0)).isNull();
        assertThat(exportState.getExportedResource(1)).isEqualTo(A);
        assertThat(exportState.getExportedAmount(1)).isEqualTo(4);
        assertThat(storage.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new ResourceAmount(A, 3));

        sut.doWork();
        assertThat(exportState.getExportedResource(0)).isNull();
        assertThat(exportState.getExportedResource(1)).isEqualTo(A);
        assertThat(exportState.getExportedAmount(1)).isEqualTo(6);
        assertThat(storage.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new ResourceAmount(A, 1));

        sut.doWork();
        assertThat(exportState.getExportedResource(0)).isNull();
        assertThat(exportState.getExportedResource(1)).isEqualTo(A);
        assertThat(exportState.getExportedAmount(1)).isEqualTo(7);
        assertThat(storage.getAll()).isEmpty();

        sut.doWork();
        sut.doWork();
        sut.doWork();
        assertThat(exportState.getExportedResource(0)).isNull();
        assertThat(exportState.getExportedResource(1)).isEqualTo(A);
        assertThat(exportState.getExportedAmount(1)).isEqualTo(7);
        assertThat(storage.getAll()).isEmpty();
    }

    @Test
    void shouldKeepExportingResourceFuzzilyUntilWantedAmountIsReached(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage
    ) {
        // Arrange
        storage.addSource(new StorageImpl());
        storage.insert(A_ALTERNATIVE, 10, Action.EXECUTE, Actor.EMPTY);
        storage.insert(A_ALTERNATIVE2, 10, Action.EXECUTE, Actor.EMPTY);

        exportState.setRequestedResource(1, A, 10);

        // Act & assert
        sut.doWork();
        assertThat(exportState.getExportedResource(0)).isNull();
        assertThat(exportState.getExportedResource(1)).isEqualTo(A_ALTERNATIVE);
        assertThat(exportState.getExportedAmount(1)).isEqualTo(2);
        assertThat(storage.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(A_ALTERNATIVE, 8),
                new ResourceAmount(A_ALTERNATIVE2, 10)
            );

        sut.doWork();
        assertThat(exportState.getExportedResource(0)).isNull();
        assertThat(exportState.getExportedResource(1)).isEqualTo(A_ALTERNATIVE);
        assertThat(exportState.getExportedAmount(1)).isEqualTo(4);
        assertThat(storage.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(A_ALTERNATIVE, 6),
                new ResourceAmount(A_ALTERNATIVE2, 10)
            );
    }

    @Test
    void shouldKeepExportingResourceFuzzilyUntilWantedAmountIsReachedEvenIfTheResourceIsNoLongerAvailableInTheNetwork(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage
    ) {
        // Arrange
        storage.addSource(new StorageImpl());
        storage.insert(A_ALTERNATIVE, 1, Action.EXECUTE, Actor.EMPTY);

        exportState.setRequestedResource(1, A, 1);

        // Act & assert
        sut.doWork();
        assertThat(exportState.getExportedResource(0)).isNull();
        assertThat(exportState.getExportedResource(1)).isEqualTo(A_ALTERNATIVE);
        assertThat(exportState.getExportedAmount(1)).isEqualTo(1);
        assertThat(storage.getAll()).isEmpty();

        sut.doWork();
        assertThat(exportState.getExportedResource(0)).isNull();
        assertThat(exportState.getExportedResource(1)).isEqualTo(A_ALTERNATIVE);
        assertThat(exportState.getExportedAmount(1)).isEqualTo(1);
        assertThat(storage.getAll()).isEmpty();
    }

    @Test
    void shouldReturnResourceToNetworkUntilWantedAmountIsReached(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage
    ) {
        // Arrange
        storage.addSource(new StorageImpl());

        exportState.setRequestedResource(1, A, 7);
        exportState.setCurrentlyExported(1, A, 10);

        // Act & assert
        sut.doWork();
        assertThat(exportState.getExportedResource(0)).isNull();
        assertThat(exportState.getExportedResource(1)).isEqualTo(A);
        assertThat(exportState.getExportedAmount(1)).isEqualTo(8);
        assertThat(storage.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new ResourceAmount(A, 2));

        sut.doWork();
        assertThat(exportState.getExportedResource(0)).isNull();
        assertThat(exportState.getExportedResource(1)).isEqualTo(A);
        assertThat(exportState.getExportedAmount(1)).isEqualTo(7);
        assertThat(storage.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new ResourceAmount(A, 3));

        sut.doWork();
        sut.doWork();
        sut.doWork();
        assertThat(exportState.getExportedResource(0)).isNull();
        assertThat(exportState.getExportedResource(1)).isEqualTo(A);
        assertThat(exportState.getExportedAmount(1)).isEqualTo(7);
        assertThat(storage.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new ResourceAmount(A, 3));
    }

    @Test
    void shouldReturnResourceToNetworkUntilWantedAmountIsReachedAndNetworkIsFull(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage
    ) {
        // Arrange
        storage.addSource(new LimitedStorageImpl(3));

        exportState.setRequestedResource(1, A, 5);
        exportState.setCurrentlyExported(1, A, 10);

        // Act & assert
        sut.doWork();
        assertThat(exportState.getExportedResource(0)).isNull();
        assertThat(exportState.getExportedResource(1)).isEqualTo(A);
        assertThat(exportState.getExportedAmount(1)).isEqualTo(8);
        assertThat(storage.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new ResourceAmount(A, 2));

        sut.doWork();
        assertThat(exportState.getExportedResource(0)).isNull();
        assertThat(exportState.getExportedResource(1)).isEqualTo(A);
        assertThat(exportState.getExportedAmount(1)).isEqualTo(7);
        assertThat(storage.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new ResourceAmount(A, 3));

        sut.doWork();
        sut.doWork();
        sut.doWork();
        assertThat(exportState.getExportedResource(0)).isNull();
        assertThat(exportState.getExportedResource(1)).isEqualTo(A);
        assertThat(exportState.getExportedAmount(1)).isEqualTo(7);
        assertThat(storage.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new ResourceAmount(A, 3));
    }

    @Test
    void shouldReturnResourceToNetworkAndExportOtherResourceIfSpecified(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage
    ) {
        // Arrange
        storage.addSource(new StorageImpl());
        storage.insert(B, 10, Action.EXECUTE, Actor.EMPTY);

        exportState.setRequestedResource(1, B, 3);
        exportState.setCurrentlyExported(1, A, 3);

        // Act & assert
        sut.doWork();
        assertThat(exportState.getExportedResource(0)).isNull();
        assertThat(exportState.getExportedResource(1)).isEqualTo(A);
        assertThat(exportState.getExportedAmount(1)).isEqualTo(1);
        assertThat(storage.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(A, 2),
                new ResourceAmount(B, 10)
            );

        sut.doWork();
        assertThat(exportState.getExportedResource(0)).isNull();
        assertThat(exportState.getExportedResource(1)).isNull();
        assertThat(storage.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(A, 3),
                new ResourceAmount(B, 10)
            );

        sut.doWork();
        assertThat(exportState.getExportedResource(0)).isNull();
        assertThat(exportState.getExportedResource(1)).isEqualTo(B);
        assertThat(exportState.getExportedAmount(1)).isEqualTo(2);
        assertThat(storage.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(A, 3),
                new ResourceAmount(B, 8)
            );

        sut.doWork();
        assertThat(exportState.getExportedResource(0)).isNull();
        assertThat(exportState.getExportedResource(1)).isEqualTo(B);
        assertThat(exportState.getExportedAmount(1)).isEqualTo(3);
        assertThat(storage.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(A, 3),
                new ResourceAmount(B, 7)
            );

        sut.doWork();
        sut.doWork();
        sut.doWork();
        assertThat(exportState.getExportedResource(0)).isNull();
        assertThat(exportState.getExportedResource(1)).isEqualTo(B);
        assertThat(exportState.getExportedAmount(1)).isEqualTo(3);
        assertThat(storage.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(A, 3),
                new ResourceAmount(B, 7)
            );
    }

    @Test
    void shouldReturnResourceToNetworkAndExportOtherResourceIfSpecifiedUntilNetworkIsFull(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage
    ) {
        // Arrange
        storage.addSource(new LimitedStorageImpl(11));
        storage.insert(B, 10, Action.EXECUTE, Actor.EMPTY);

        exportState.setRequestedResource(1, B, 3);
        exportState.setCurrentlyExported(1, A, 3);

        // Act & assert
        sut.doWork();
        assertThat(exportState.getExportedResource(0)).isNull();
        assertThat(exportState.getExportedResource(1)).isEqualTo(A);
        assertThat(exportState.getExportedAmount(1)).isEqualTo(2);
        assertThat(storage.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(A, 1),
                new ResourceAmount(B, 10)
            );

        sut.doWork();
        sut.doWork();
        sut.doWork();
        assertThat(exportState.getExportedResource(0)).isNull();
        assertThat(exportState.getExportedResource(1)).isEqualTo(A);
        assertThat(exportState.getExportedAmount(1)).isEqualTo(2);
        assertThat(storage.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(A, 1),
                new ResourceAmount(B, 10)
            );
    }
}
