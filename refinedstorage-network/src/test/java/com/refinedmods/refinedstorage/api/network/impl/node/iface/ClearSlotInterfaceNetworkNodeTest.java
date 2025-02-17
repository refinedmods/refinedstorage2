package com.refinedmods.refinedstorage.api.network.impl.node.iface;

import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.storage.StorageImpl;
import com.refinedmods.refinedstorage.api.storage.limited.LimitedStorageImpl;
import com.refinedmods.refinedstorage.network.test.AddNetworkNode;
import com.refinedmods.refinedstorage.network.test.InjectNetworkStorageComponent;
import com.refinedmods.refinedstorage.network.test.NetworkTest;
import com.refinedmods.refinedstorage.network.test.SetupNetwork;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.A;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.B;
import static org.assertj.core.api.Assertions.assertThat;

@NetworkTest
@SetupNetwork
class ClearSlotInterfaceNetworkNodeTest {
    @AddNetworkNode
    InterfaceNetworkNode sut;

    private InterfaceExportStateImpl exportState;

    @BeforeEach
    void setUp() {
        exportState = new InterfaceExportStateImpl(3);
        sut.setExportState(exportState);
        sut.setTransferQuotaProvider(resource -> 2);
        sut.setEnergyUsage(5);
    }

    @Test
    void shouldClearSlotWhenNoLongerRequestingAnything(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage
    ) {
        // Arrange
        storage.addSource(new StorageImpl());

        exportState.setCurrentlyExported(1, A, 7);
        exportState.setCurrentlyExported(2, B, 2);

        // Act & assert
        sut.doWork();
        assertThat(exportState.getExportedResource(0)).isNull();
        assertThat(exportState.getExportedResource(1)).isEqualTo(A);
        assertThat(exportState.getExportedAmount(1)).isEqualTo(5);
        assertThat(exportState.getExportedResource(2)).isNull();
        assertThat(storage.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(A, 2),
                new ResourceAmount(B, 2)
            );

        sut.doWork();
        assertThat(exportState.getExportedResource(0)).isNull();
        assertThat(exportState.getExportedResource(1)).isEqualTo(A);
        assertThat(exportState.getExportedAmount(1)).isEqualTo(3);
        assertThat(exportState.getExportedResource(2)).isNull();
        assertThat(storage.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(A, 4),
                new ResourceAmount(B, 2)
            );

        sut.doWork();
        assertThat(exportState.getExportedResource(0)).isNull();
        assertThat(exportState.getExportedResource(1)).isEqualTo(A);
        assertThat(exportState.getExportedAmount(1)).isEqualTo(1);
        assertThat(exportState.getExportedResource(2)).isNull();
        assertThat(storage.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(A, 6),
                new ResourceAmount(B, 2)
            );

        sut.doWork();
        assertThat(exportState.getExportedResource(0)).isNull();
        assertThat(exportState.getExportedResource(1)).isNull();
        assertThat(exportState.getExportedResource(2)).isNull();
        assertThat(storage.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(A, 7),
                new ResourceAmount(B, 2)
            );

        sut.doWork();
        sut.doWork();
        sut.doWork();
        assertThat(exportState.getExportedResource(0)).isNull();
        assertThat(exportState.getExportedResource(1)).isNull();
        assertThat(exportState.getExportedResource(2)).isNull();
        assertThat(storage.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(A, 7),
                new ResourceAmount(B, 2)
            );
    }

    @Test
    void shouldClearSlotPartiallyWhenNoLongerRequestingAnythingButNetworkDoesNotHaveEnoughSpace(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage
    ) {
        // Arrange
        storage.addSource(new LimitedStorageImpl(3));

        exportState.setCurrentlyExported(1, A, 7);

        // Act & assert
        sut.doWork();
        assertThat(exportState.getExportedResource(0)).isNull();
        assertThat(exportState.getExportedResource(1)).isEqualTo(A);
        assertThat(exportState.getExportedAmount(1)).isEqualTo(5);
        assertThat(exportState.getExportedResource(2)).isNull();
        assertThat(storage.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new ResourceAmount(A, 2));

        sut.doWork();
        assertThat(exportState.getExportedResource(0)).isNull();
        assertThat(exportState.getExportedResource(1)).isEqualTo(A);
        assertThat(exportState.getExportedAmount(1)).isEqualTo(4);
        assertThat(exportState.getExportedResource(2)).isNull();
        assertThat(storage.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new ResourceAmount(A, 3));

        sut.doWork();
        sut.doWork();
        sut.doWork();
        assertThat(exportState.getExportedResource(0)).isNull();
        assertThat(exportState.getExportedResource(1)).isEqualTo(A);
        assertThat(exportState.getExportedAmount(1)).isEqualTo(4);
        assertThat(exportState.getExportedResource(2)).isNull();
        assertThat(storage.getAll())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new ResourceAmount(A, 3));
    }

    @Test
    void shouldNotClearSlotWhenNoLongerRequestingAnythingAndNetworkDoesNotHaveEnoughSpace(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage
    ) {
        // Arrange
        exportState.setCurrentlyExported(1, A, 7);

        // Act
        sut.doWork();

        // Assert
        assertThat(exportState.getExportedResource(0)).isNull();
        assertThat(exportState.getExportedResource(1)).isEqualTo(A);
        assertThat(exportState.getExportedAmount(1)).isEqualTo(7);
        assertThat(exportState.getExportedResource(2)).isNull();

        assertThat(storage.getAll()).isEmpty();
    }
}
