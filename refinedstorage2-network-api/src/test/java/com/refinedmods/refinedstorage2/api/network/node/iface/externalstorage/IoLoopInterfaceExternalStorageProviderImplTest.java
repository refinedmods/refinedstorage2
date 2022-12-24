package com.refinedmods.refinedstorage2.api.network.node.iface.externalstorage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.node.externalstorage.ExternalStorageNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.externalstorage.ExternalStorageProviderFactoryImpl;
import com.refinedmods.refinedstorage2.api.network.node.externalstorage.StorageExternalStorageProvider;
import com.refinedmods.refinedstorage2.api.network.node.iface.InterfaceExportStateImpl;
import com.refinedmods.refinedstorage2.api.network.node.iface.InterfaceNetworkNode;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.EmptyActor;
import com.refinedmods.refinedstorage2.api.storage.InMemoryStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.network.test.AddNetworkNode;
import com.refinedmods.refinedstorage2.network.test.InjectNetwork;
import com.refinedmods.refinedstorage2.network.test.InjectNetworkStorageChannel;
import com.refinedmods.refinedstorage2.network.test.NetworkTest;
import com.refinedmods.refinedstorage2.network.test.SetupNetwork;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@NetworkTest
@SetupNetwork
public class IoLoopInterfaceExternalStorageProviderImplTest {
    Storage<String> regularStorageInNetwork;

    @AddNetworkNode
    InterfaceNetworkNode<String> interfaceWithExternalStorage;
    InterfaceExportStateImpl interfaceWithExternalStorageState;
    @AddNetworkNode
    ExternalStorageNetworkNode externalStorageConnectionToInterface;

    @AddNetworkNode
    InterfaceNetworkNode<String> interfaceWithExternalStorage2;
    InterfaceExportStateImpl interfaceWithExternalStorageState2;
    @AddNetworkNode
    ExternalStorageNetworkNode externalStorageConnectionToInterface2;

    @AddNetworkNode
    InterfaceNetworkNode<String> regularInterface;
    InterfaceExportStateImpl regularInterfaceState;

    // this has no usages, but it's useful to bring an external storage in the network ctx that has no delegate
    @AddNetworkNode
    ExternalStorageNetworkNode externalStorageWithoutConnection;

    @AddNetworkNode
    ExternalStorageNetworkNode externalStorageWithNonInterfaceConnection;

    @BeforeEach
    void setUp(@InjectNetworkStorageChannel final StorageChannel<String> networkStorage) {
        interfaceWithExternalStorageState = new InterfaceExportStateImpl(2);
        interfaceWithExternalStorageState.setRequestedResource(1, "A", 10);
        interfaceWithExternalStorage.setExportState(interfaceWithExternalStorageState);
        interfaceWithExternalStorage.setTransferQuota(100);
        externalStorageConnectionToInterface.initialize(new ExternalStorageProviderFactoryImpl(
            new InterfaceExternalStorageProviderImpl<>(interfaceWithExternalStorage)
        ));

        interfaceWithExternalStorageState2 = new InterfaceExportStateImpl(2);
        interfaceWithExternalStorageState2.setRequestedResource(1, "A", 10);
        interfaceWithExternalStorage2.setExportState(interfaceWithExternalStorageState2);
        interfaceWithExternalStorage2.setTransferQuota(100);
        externalStorageConnectionToInterface2.initialize(new ExternalStorageProviderFactoryImpl(
            new InterfaceExternalStorageProviderImpl<>(interfaceWithExternalStorage2)
        ));

        regularInterfaceState = new InterfaceExportStateImpl(2);
        regularInterfaceState.setRequestedResource(1, "A", 10);
        regularInterface.setExportState(regularInterfaceState);
        regularInterface.setTransferQuota(100);

        regularStorageInNetwork = new InMemoryStorageImpl<>();
        regularStorageInNetwork.insert("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);
        networkStorage.addSource(regularStorageInNetwork);

        externalStorageWithNonInterfaceConnection.initialize(new ExternalStorageProviderFactoryImpl(
            new StorageExternalStorageProvider<>(new InMemoryStorageImpl<>())
        ));
    }

    // Insertions
    // from Interfaces acting as External Storage
    // to other Interfaces acting as External Storage
    // isn't allowed as it would create an insertion loop between the Interfaces for unwanted items
    // and would double count them because the External Storage update is later.
    @Test
    void shouldNotAllowInsertionByAnotherInterfaceIfThatInterfaceIsActingAsExternalStorage(
        @InjectNetworkStorageChannel final StorageChannel<String> networkStorage,
        @InjectNetwork final Network network
    ) {
        // Arrange
        networkStorage.removeSource(regularStorageInNetwork);
        network.removeContainer(() -> externalStorageWithNonInterfaceConnection);

        interfaceWithExternalStorageState.setCurrentlyExported(0, "A", 15);
        interfaceWithExternalStorageState.clearRequestedResources();

        // Act
        // first do the external storage update as it's important for the double counting aspect
        externalStorageConnectionToInterface.detectChanges();
        interfaceWithExternalStorage.doWork();

        // Assert
        assertThat(interfaceWithExternalStorageState.getCurrentlyExportedResource(0)).isEqualTo("A");
        assertThat(interfaceWithExternalStorageState.getCurrentlyExportedResourceAmount(0)).isEqualTo(15);

        assertThat(interfaceWithExternalStorageState2.getCurrentlyExportedResource(0)).isNull();
        assertThat(interfaceWithExternalStorageState2.getCurrentlyExportedResourceAmount(0)).isZero();

        assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("A", 15)
        );
    }

    @Test
    void shouldAllowInsertionByAnotherInterfaceIfThatInterfaceIsNotActingAsExternalStorage(
        @InjectNetworkStorageChannel final StorageChannel<String> networkStorage,
        @InjectNetwork final Network network
    ) {
        // Arrange
        networkStorage.removeSource(regularStorageInNetwork);
        network.removeContainer(() -> externalStorageWithNonInterfaceConnection);

        regularInterfaceState.setCurrentlyExported(0, "A", 10);
        regularInterfaceState.clearRequestedResources();

        // Act
        regularInterface.doWork();

        // Assert
        assertThat(interfaceWithExternalStorageState.getCurrentlyExportedResource(0)).isEqualTo("A");
        assertThat(interfaceWithExternalStorageState.getCurrentlyExportedResourceAmount(0)).isEqualTo(10);

        assertThat(interfaceWithExternalStorageState2.getCurrentlyExportedResource(0)).isNull();
        assertThat(interfaceWithExternalStorageState2.getCurrentlyExportedResourceAmount(0)).isZero();

        assertThat(regularInterfaceState.getCurrentlyExportedResource(1)).isNull();
        assertThat(regularInterfaceState.getCurrentlyExportedResourceAmount(1)).isZero();

        assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("A", 10)
        );
    }

    // Extractions
    // originating from Interfaces acting as External Storage
    // extracting from other Interfaces acting as External Storage
    // isn't allowed as it would create an extraction loop causing the Interfaces to constantly steal from each other.
    @Test
    void shouldNotAllowExtractionRequestedByAnotherInterfaceIfThatInterfaceIsActingAsExternalStorage(
        @InjectNetworkStorageChannel final StorageChannel<String> networkStorage
    ) {
        // Act & assert
        interfaceWithExternalStorage.doWork();
        externalStorageConnectionToInterface.detectChanges();

        assertThat(interfaceWithExternalStorageState.getCurrentlyExportedResource(0)).isNull();
        assertThat(interfaceWithExternalStorageState.getCurrentlyExportedResource(1)).isEqualTo("A");
        assertThat(interfaceWithExternalStorageState.getCurrentlyExportedResourceAmount(1)).isEqualTo(10);

        assertThat(interfaceWithExternalStorageState2.getCurrentlyExportedResource(0)).isNull();
        assertThat(interfaceWithExternalStorageState2.getCurrentlyExportedResource(1)).isNull();
        assertThat(interfaceWithExternalStorageState2.getCurrentlyExportedResourceAmount(1)).isZero();

        assertThat(regularStorageInNetwork.getAll()).isEmpty();
        assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("A", 10)
        );

        interfaceWithExternalStorage2.doWork();
        externalStorageConnectionToInterface2.detectChanges();

        assertThat(interfaceWithExternalStorageState.getCurrentlyExportedResource(0)).isNull();
        assertThat(interfaceWithExternalStorageState.getCurrentlyExportedResource(1)).isEqualTo("A");
        assertThat(interfaceWithExternalStorageState.getCurrentlyExportedResourceAmount(1)).isEqualTo(10);

        assertThat(interfaceWithExternalStorageState2.getCurrentlyExportedResource(0)).isNull();
        assertThat(interfaceWithExternalStorageState2.getCurrentlyExportedResource(1)).isNull();
        assertThat(interfaceWithExternalStorageState2.getCurrentlyExportedResourceAmount(1)).isZero();

        assertThat(regularStorageInNetwork.getAll()).isEmpty();
        assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("A", 10)
        );
    }

    @Test
    void shouldAllowExtractionRequestedByAnotherInterfaceIfThatInterfaceIsNotActingAsExternalStorage(
        @InjectNetworkStorageChannel final StorageChannel<String> networkStorage
    ) {
        // Act & assert
        interfaceWithExternalStorage.doWork();
        externalStorageConnectionToInterface.detectChanges();

        assertThat(interfaceWithExternalStorageState.getCurrentlyExportedResource(0)).isNull();
        assertThat(interfaceWithExternalStorageState.getCurrentlyExportedResource(1)).isEqualTo("A");
        assertThat(interfaceWithExternalStorageState.getCurrentlyExportedResourceAmount(1)).isEqualTo(10);

        assertThat(regularInterfaceState.getCurrentlyExportedResource(0)).isNull();
        assertThat(regularInterfaceState.getCurrentlyExportedResource(1)).isNull();
        assertThat(regularInterfaceState.getCurrentlyExportedResourceAmount(1)).isZero();

        assertThat(regularStorageInNetwork.getAll()).isEmpty();
        assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("A", 10)
        );

        regularInterface.doWork();
        // update these also to ensure that they don't steal back.
        interfaceWithExternalStorage.doWork();
        externalStorageConnectionToInterface.detectChanges();

        assertThat(interfaceWithExternalStorageState.getCurrentlyExportedResource(0)).isNull();
        assertThat(interfaceWithExternalStorageState.getCurrentlyExportedResource(1)).isNull();
        assertThat(interfaceWithExternalStorageState.getCurrentlyExportedResourceAmount(1)).isZero();

        assertThat(regularInterfaceState.getCurrentlyExportedResource(0)).isNull();
        assertThat(regularInterfaceState.getCurrentlyExportedResource(1)).isEqualTo("A");
        assertThat(regularInterfaceState.getCurrentlyExportedResourceAmount(1)).isEqualTo(10);

        assertThat(regularStorageInNetwork.getAll()).isEmpty();
        assertThat(networkStorage.getAll()).isEmpty();
    }
}
