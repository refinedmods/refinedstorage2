package com.refinedmods.refinedstorage.api.network.impl.node.iface.externalstorage;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.impl.node.externalstorage.ExternalStorageNetworkNode;
import com.refinedmods.refinedstorage.api.network.impl.node.externalstorage.ExternalStorageProviderFactoryImpl;
import com.refinedmods.refinedstorage.api.network.impl.node.iface.InterfaceExportStateImpl;
import com.refinedmods.refinedstorage.api.network.impl.node.iface.InterfaceNetworkNode;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.storage.EmptyActor;
import com.refinedmods.refinedstorage.network.test.AddNetworkNode;
import com.refinedmods.refinedstorage.network.test.InjectNetworkStorageComponent;
import com.refinedmods.refinedstorage.network.test.NetworkTest;
import com.refinedmods.refinedstorage.network.test.SetupNetwork;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static com.refinedmods.refinedstorage.network.test.fake.FakeResources.A;
import static com.refinedmods.refinedstorage.network.test.fake.FakeResources.B;
import static org.assertj.core.api.Assertions.assertThat;

@NetworkTest
@SetupNetwork
class InterfaceExternalStorageProviderImplTest {
    @AddNetworkNode
    InterfaceNetworkNode interfaceNetworkNode;
    @AddNetworkNode
    InterfaceNetworkNode interfaceNetworkNodeWithoutExportState;
    @AddNetworkNode
    ExternalStorageNetworkNode externalStorage;

    InterfaceExportStateImpl exportState;

    @BeforeEach
    void setUp() {
        exportState = new InterfaceExportStateImpl(9);
        interfaceNetworkNode.setExportState(exportState);
    }

    @Test
    void shouldExposeExportedResources(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage
    ) {
        // Arrange
        exportState.setCurrentlyExported(0, A, 100);
        exportState.setCurrentlyExported(8, A, 1);

        externalStorage.initialize(new ExternalStorageProviderFactoryImpl(new InterfaceExternalStorageProviderImpl(
            interfaceNetworkNode
        )));

        // Act
        externalStorage.detectChanges();

        // Assert
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 101)
        );
    }

    @Test
    void shouldNotExposeExportedResourceWithoutExportState(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage
    ) {
        // Arrange
        externalStorage.initialize(new ExternalStorageProviderFactoryImpl(new InterfaceExternalStorageProviderImpl(
            interfaceNetworkNodeWithoutExportState
        )));

        // Act
        externalStorage.detectChanges();

        // Assert
        assertThat(storage.getAll()).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldInsertIntoInterface(
        final Action action,
        @InjectNetworkStorageComponent final StorageNetworkComponent storage
    ) {
        // Arrange
        externalStorage.initialize(new ExternalStorageProviderFactoryImpl(new InterfaceExternalStorageProviderImpl(
            interfaceNetworkNode
        )));

        // Act
        final long inserted = storage.insert(A, 10, action, EmptyActor.INSTANCE);

        // Assert
        assertThat(inserted).isEqualTo(10);
        if (action == Action.EXECUTE) {
            assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 10)
            );
            assertThat(exportState.getExportedResource(0)).isEqualTo(A);
            assertThat(exportState.getExportedAmount(0)).isEqualTo(10);
        } else {
            assertThat(storage.getAll()).isEmpty();
            assertThat(exportState.getExportedResource(0)).isNull();
            assertThat(exportState.getExportedAmount(0)).isZero();
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldNotInsertResourceWithoutExportState(
        final Action action,
        @InjectNetworkStorageComponent final StorageNetworkComponent storage
    ) {
        // Arrange
        externalStorage.initialize(new ExternalStorageProviderFactoryImpl(new InterfaceExternalStorageProviderImpl(
            interfaceNetworkNodeWithoutExportState
        )));
        externalStorage.detectChanges();

        // Act
        final long inserted = storage.insert(A, 101, action, EmptyActor.INSTANCE);

        // Assert
        assertThat(inserted).isZero();
        assertThat(storage.getAll()).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldExtractEntireResourceFromInterface(
        final Action action,
        @InjectNetworkStorageComponent final StorageNetworkComponent storage
    ) {
        // Arrange
        exportState.setCurrentlyExported(0, A, 50);
        exportState.setCurrentlyExported(1, A, 50);
        externalStorage.initialize(new ExternalStorageProviderFactoryImpl(new InterfaceExternalStorageProviderImpl(
            interfaceNetworkNode
        )));
        externalStorage.detectChanges();

        // Act
        final long extracted = storage.extract(A, 101, action, EmptyActor.INSTANCE);

        // Assert
        assertThat(extracted).isEqualTo(100);
        if (action == Action.EXECUTE) {
            assertThat(storage.getAll()).isEmpty();
            assertThat(exportState.getExportedResource(0)).isNull();
            assertThat(exportState.getExportedAmount(0)).isZero();
            assertThat(exportState.getExportedResource(1)).isNull();
            assertThat(exportState.getExportedAmount(1)).isZero();
        } else {
            assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 100)
            );
            assertThat(exportState.getExportedResource(0)).isEqualTo(A);
            assertThat(exportState.getExportedAmount(0)).isEqualTo(50);
            assertThat(exportState.getExportedResource(1)).isEqualTo(A);
            assertThat(exportState.getExportedAmount(1)).isEqualTo(50);
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldExtractPartialResourceFromInterface(
        final Action action,
        @InjectNetworkStorageComponent final StorageNetworkComponent storage
    ) {
        // Arrange
        exportState.setCurrentlyExported(0, A, 50);
        exportState.setCurrentlyExported(1, A, 50);
        externalStorage.initialize(new ExternalStorageProviderFactoryImpl(new InterfaceExternalStorageProviderImpl(
            interfaceNetworkNode
        )));
        externalStorage.detectChanges();

        // Act
        final long extracted = storage.extract(A, 51, action, EmptyActor.INSTANCE);

        // Assert
        assertThat(extracted).isEqualTo(51);
        if (action == Action.EXECUTE) {
            assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 49)
            );
            assertThat(exportState.getExportedResource(0)).isNull();
            assertThat(exportState.getExportedAmount(0)).isZero();
            assertThat(exportState.getExportedResource(1)).isEqualTo(A);
            assertThat(exportState.getExportedAmount(1)).isEqualTo(49);
        } else {
            assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 100)
            );
            assertThat(exportState.getExportedResource(0)).isEqualTo(A);
            assertThat(exportState.getExportedAmount(0)).isEqualTo(50);
            assertThat(exportState.getExportedResource(1)).isEqualTo(A);
            assertThat(exportState.getExportedAmount(1)).isEqualTo(50);
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldNotExtractNonExistentResourceFromInterface(
        final Action action,
        @InjectNetworkStorageComponent final StorageNetworkComponent storage
    ) {
        // Arrange
        exportState.setCurrentlyExported(0, A, 50);
        externalStorage.initialize(new ExternalStorageProviderFactoryImpl(new InterfaceExternalStorageProviderImpl(
            interfaceNetworkNode
        )));
        externalStorage.detectChanges();

        // Act
        final long extracted = storage.extract(B, 1, action, EmptyActor.INSTANCE);

        // Assert
        assertThat(extracted).isZero();
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 50)
        );
        assertThat(exportState.getExportedResource(0)).isEqualTo(A);
        assertThat(exportState.getExportedAmount(0)).isEqualTo(50);
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldNotExtractResourceWithoutExportState(
        final Action action,
        @InjectNetworkStorageComponent final StorageNetworkComponent storage
    ) {
        // Arrange
        externalStorage.initialize(new ExternalStorageProviderFactoryImpl(new InterfaceExternalStorageProviderImpl(
            interfaceNetworkNodeWithoutExportState
        )));
        externalStorage.detectChanges();

        // Act
        final long extracted = storage.extract(A, 101, action, EmptyActor.INSTANCE);

        // Assert
        assertThat(extracted).isZero();
        assertThat(storage.getAll()).isEmpty();
    }
}
