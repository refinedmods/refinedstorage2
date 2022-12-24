package com.refinedmods.refinedstorage2.api.network.node.iface.externalstorage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.node.externalstorage.ExternalStorageNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.externalstorage.ExternalStorageProviderFactoryImpl;
import com.refinedmods.refinedstorage2.api.network.node.iface.InterfaceExportStateImpl;
import com.refinedmods.refinedstorage2.api.network.node.iface.InterfaceNetworkNode;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.EmptyActor;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.network.test.AddNetworkNode;
import com.refinedmods.refinedstorage2.network.test.InjectNetworkStorageChannel;
import com.refinedmods.refinedstorage2.network.test.NetworkTest;
import com.refinedmods.refinedstorage2.network.test.SetupNetwork;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

@NetworkTest
@SetupNetwork
class InterfaceExternalStorageProviderImplTest {
    @AddNetworkNode
    InterfaceNetworkNode<String> interfaceNetworkNode;
    @AddNetworkNode
    InterfaceNetworkNode<String> interfaceNetworkNodeWithoutExportState;
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
        @InjectNetworkStorageChannel final StorageChannel<String> networkStorage
    ) {
        // Arrange
        exportState.setCurrentlyExported(0, "A", 100);
        exportState.setCurrentlyExported(8, "A", 1);

        externalStorage.initialize(new ExternalStorageProviderFactoryImpl(
            new InterfaceExternalStorageProviderImpl<>(interfaceNetworkNode)
        ));

        // Act
        externalStorage.detectChanges();

        // Assert
        assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("A", 101)
        );
    }

    @Test
    void shouldNotExposeExportedResourceWithoutExportState(
        @InjectNetworkStorageChannel final StorageChannel<String> networkStorage
    ) {
        // Arrange
        externalStorage.initialize(new ExternalStorageProviderFactoryImpl(
            new InterfaceExternalStorageProviderImpl<>(interfaceNetworkNodeWithoutExportState)
        ));

        // Act
        externalStorage.detectChanges();

        // Assert
        assertThat(networkStorage.getAll()).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldInsertIntoInterface(
        final Action action,
        @InjectNetworkStorageChannel final StorageChannel<String> networkStorage
    ) {
        // Arrange
        externalStorage.initialize(new ExternalStorageProviderFactoryImpl(
            new InterfaceExternalStorageProviderImpl<>(interfaceNetworkNode)
        ));

        // Act
        final long inserted = networkStorage.insert("A", 10, action, EmptyActor.INSTANCE);

        // Assert
        assertThat(inserted).isEqualTo(10);
        if (action == Action.EXECUTE) {
            assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount<>("A", 10)
            );
            assertThat(exportState.getCurrentlyExportedResource(0)).isEqualTo("A");
            assertThat(exportState.getCurrentlyExportedResourceAmount(0)).isEqualTo(10);
        } else {
            assertThat(networkStorage.getAll()).isEmpty();
            assertThat(exportState.getCurrentlyExportedResource(0)).isNull();
            assertThat(exportState.getCurrentlyExportedResourceAmount(0)).isZero();
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldNotInsertResourceWithoutExportState(
        final Action action,
        @InjectNetworkStorageChannel final StorageChannel<String> networkStorage
    ) {
        // Arrange
        externalStorage.initialize(new ExternalStorageProviderFactoryImpl(
            new InterfaceExternalStorageProviderImpl<>(interfaceNetworkNodeWithoutExportState)
        ));
        externalStorage.detectChanges();

        // Act
        final long inserted = networkStorage.insert("A", 101, action, EmptyActor.INSTANCE);

        // Assert
        assertThat(inserted).isZero();
        assertThat(networkStorage.getAll()).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldExtractEntireResourceFromInterface(
        final Action action,
        @InjectNetworkStorageChannel final StorageChannel<String> networkStorage
    ) {
        // Arrange
        exportState.setCurrentlyExported(0, "A", 50);
        exportState.setCurrentlyExported(1, "A", 50);
        externalStorage.initialize(new ExternalStorageProviderFactoryImpl(
            new InterfaceExternalStorageProviderImpl<>(interfaceNetworkNode)
        ));
        externalStorage.detectChanges();

        // Act
        final long extracted = networkStorage.extract("A", 101, action, EmptyActor.INSTANCE);

        // Assert
        assertThat(extracted).isEqualTo(100);
        if (action == Action.EXECUTE) {
            assertThat(networkStorage.getAll()).isEmpty();
            assertThat(exportState.getCurrentlyExportedResource(0)).isNull();
            assertThat(exportState.getCurrentlyExportedResourceAmount(0)).isZero();
            assertThat(exportState.getCurrentlyExportedResource(1)).isNull();
            assertThat(exportState.getCurrentlyExportedResourceAmount(1)).isZero();
        } else {
            assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount<>("A", 100)
            );
            assertThat(exportState.getCurrentlyExportedResource(0)).isEqualTo("A");
            assertThat(exportState.getCurrentlyExportedResourceAmount(0)).isEqualTo(50);
            assertThat(exportState.getCurrentlyExportedResource(1)).isEqualTo("A");
            assertThat(exportState.getCurrentlyExportedResourceAmount(1)).isEqualTo(50);
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldExtractPartialResourceFromInterface(
        final Action action,
        @InjectNetworkStorageChannel final StorageChannel<String> networkStorage
    ) {
        // Arrange
        exportState.setCurrentlyExported(0, "A", 50);
        exportState.setCurrentlyExported(1, "A", 50);
        externalStorage.initialize(new ExternalStorageProviderFactoryImpl(
            new InterfaceExternalStorageProviderImpl<>(interfaceNetworkNode)
        ));
        externalStorage.detectChanges();

        // Act
        final long extracted = networkStorage.extract("A", 51, action, EmptyActor.INSTANCE);

        // Assert
        assertThat(extracted).isEqualTo(51);
        if (action == Action.EXECUTE) {
            assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount<>("A", 49)
            );
            assertThat(exportState.getCurrentlyExportedResource(0)).isNull();
            assertThat(exportState.getCurrentlyExportedResourceAmount(0)).isZero();
            assertThat(exportState.getCurrentlyExportedResource(1)).isEqualTo("A");
            assertThat(exportState.getCurrentlyExportedResourceAmount(1)).isEqualTo(49);
        } else {
            assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount<>("A", 100)
            );
            assertThat(exportState.getCurrentlyExportedResource(0)).isEqualTo("A");
            assertThat(exportState.getCurrentlyExportedResourceAmount(0)).isEqualTo(50);
            assertThat(exportState.getCurrentlyExportedResource(1)).isEqualTo("A");
            assertThat(exportState.getCurrentlyExportedResourceAmount(1)).isEqualTo(50);
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldNotExtractNonExistentResourceFromInterface(
        final Action action,
        @InjectNetworkStorageChannel final StorageChannel<String> networkStorage
    ) {
        // Arrange
        exportState.setCurrentlyExported(0, "A", 50);
        externalStorage.initialize(new ExternalStorageProviderFactoryImpl(
            new InterfaceExternalStorageProviderImpl<>(interfaceNetworkNode)
        ));
        externalStorage.detectChanges();

        // Act
        final long extracted = networkStorage.extract("B", 1, action, EmptyActor.INSTANCE);

        // Assert
        assertThat(extracted).isZero();
        assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("A", 50)
        );
        assertThat(exportState.getCurrentlyExportedResource(0)).isEqualTo("A");
        assertThat(exportState.getCurrentlyExportedResourceAmount(0)).isEqualTo(50);
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldNotExtractResourceWithoutExportState(
        final Action action,
        @InjectNetworkStorageChannel final StorageChannel<String> networkStorage
    ) {
        // Arrange
        externalStorage.initialize(new ExternalStorageProviderFactoryImpl(
            new InterfaceExternalStorageProviderImpl<>(interfaceNetworkNodeWithoutExportState)
        ));
        externalStorage.detectChanges();

        // Act
        final long extracted = networkStorage.extract("A", 101, action, EmptyActor.INSTANCE);

        // Assert
        assertThat(extracted).isZero();
        assertThat(networkStorage.getAll()).isEmpty();
    }
}
