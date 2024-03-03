package com.refinedmods.refinedstorage2.api.network.impl.node.iface.externalstorage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.impl.node.externalstorage.ExternalStorageNetworkNode;
import com.refinedmods.refinedstorage2.api.network.impl.node.externalstorage.ExternalStorageProviderFactoryImpl;
import com.refinedmods.refinedstorage2.api.network.impl.node.externalstorage.StorageExternalStorageProvider;
import com.refinedmods.refinedstorage2.api.network.impl.node.iface.InterfaceExportStateImpl;
import com.refinedmods.refinedstorage2.api.network.impl.node.iface.InterfaceNetworkNode;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.EmptyActor;
import com.refinedmods.refinedstorage2.api.storage.InMemoryStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.ResourceTemplate;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.network.test.AddNetworkNode;
import com.refinedmods.refinedstorage2.network.test.InjectNetwork;
import com.refinedmods.refinedstorage2.network.test.InjectNetworkStorageChannel;
import com.refinedmods.refinedstorage2.network.test.NetworkTest;
import com.refinedmods.refinedstorage2.network.test.NetworkTestFixtures;
import com.refinedmods.refinedstorage2.network.test.SetupNetwork;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage2.network.test.TestResourceKey.A;
import static org.assertj.core.api.Assertions.assertThat;

@NetworkTest
@SetupNetwork
class IoLoopInterfaceExternalStorageProviderImplTest {
    Storage regularStorageInNetwork;

    @AddNetworkNode
    InterfaceNetworkNode interfaceWithExternalStorage;
    InterfaceExportStateImpl interfaceWithExternalStorageState;
    @AddNetworkNode
    ExternalStorageNetworkNode externalStorageConnectionToInterface;

    @AddNetworkNode
    InterfaceNetworkNode interfaceWithExternalStorage2;
    InterfaceExportStateImpl interfaceWithExternalStorageState2;
    @AddNetworkNode
    ExternalStorageNetworkNode externalStorageConnectionToInterface2;

    @AddNetworkNode
    InterfaceNetworkNode regularInterface;
    InterfaceExportStateImpl regularInterfaceState;

    // This has no usages, but it's useful to bring an external storage in the network context that has no delegate
    @AddNetworkNode
    @SuppressWarnings("unused")
    ExternalStorageNetworkNode externalStorageWithoutConnection;

    @AddNetworkNode
    ExternalStorageNetworkNode externalStorageWithNonInterfaceConnection;

    @BeforeEach
    void setUp(@InjectNetworkStorageChannel final StorageChannel networkStorage) {
        interfaceWithExternalStorageState = new InterfaceExportStateImpl(2);
        interfaceWithExternalStorageState.setRequestedResource(1, A, 10);
        interfaceWithExternalStorage.setExportState(interfaceWithExternalStorageState);
        interfaceWithExternalStorage.setTransferQuotaProvider(resource -> 100);
        externalStorageConnectionToInterface.initialize(new ExternalStorageProviderFactoryImpl(
            new InterfaceExternalStorageProviderImpl(
                interfaceWithExternalStorage,
                NetworkTestFixtures.STORAGE_CHANNEL_TYPE
            )
        ));

        interfaceWithExternalStorageState2 = new InterfaceExportStateImpl(2);
        interfaceWithExternalStorageState2.setRequestedResource(1, A, 10);
        interfaceWithExternalStorage2.setExportState(interfaceWithExternalStorageState2);
        interfaceWithExternalStorage2.setTransferQuotaProvider(resource -> 100);
        externalStorageConnectionToInterface2.initialize(new ExternalStorageProviderFactoryImpl(
            new InterfaceExternalStorageProviderImpl(
                interfaceWithExternalStorage2,
                NetworkTestFixtures.STORAGE_CHANNEL_TYPE
            )
        ));

        regularInterfaceState = new InterfaceExportStateImpl(2);
        regularInterfaceState.setRequestedResource(1, A, 10);
        regularInterface.setExportState(regularInterfaceState);
        regularInterface.setTransferQuotaProvider(resource -> 100);

        regularStorageInNetwork = new InMemoryStorageImpl();
        regularStorageInNetwork.insert(A, 10, Action.EXECUTE, EmptyActor.INSTANCE);
        networkStorage.addSource(regularStorageInNetwork);

        externalStorageWithNonInterfaceConnection.initialize(new ExternalStorageProviderFactoryImpl(
            new StorageExternalStorageProvider(new InMemoryStorageImpl())
        ));
    }

    // Insertions
    // from Interfaces acting as External Storage
    // to other Interfaces acting as External Storage
    // isn't allowed as it would create an insertion loop between the Interfaces for unwanted resources
    // and would double count them because the External Storage update is later.
    @Test
    void shouldNotAllowInsertionByAnotherInterfaceIfThatInterfaceIsActingAsExternalStorage(
        @InjectNetworkStorageChannel final StorageChannel networkStorage,
        @InjectNetwork final Network network
    ) {
        // Arrange
        networkStorage.removeSource(regularStorageInNetwork);
        network.removeContainer(() -> externalStorageWithNonInterfaceConnection);

        interfaceWithExternalStorageState.setCurrentlyExported(0, A, 15);
        interfaceWithExternalStorageState.clearRequestedResources();

        // Act
        // first do the external storage update as it's important for the double counting aspect
        externalStorageConnectionToInterface.detectChanges();
        interfaceWithExternalStorage.doWork();

        // Assert
        assertThat(interfaceWithExternalStorageState.getExportedResource(0))
            .usingRecursiveComparison()
            .isEqualTo(new ResourceTemplate(A, NetworkTestFixtures.STORAGE_CHANNEL_TYPE));
        assertThat(interfaceWithExternalStorageState.getExportedAmount(0)).isEqualTo(15);

        assertThat(interfaceWithExternalStorageState2.getExportedResource(0)).isNull();
        assertThat(interfaceWithExternalStorageState2.getExportedAmount(0)).isZero();

        assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 15)
        );
    }

    @Test
    void shouldAllowInsertionByAnotherInterfaceIfThatInterfaceIsNotActingAsExternalStorage(
        @InjectNetworkStorageChannel final StorageChannel networkStorage,
        @InjectNetwork final Network network
    ) {
        // Arrange
        networkStorage.removeSource(regularStorageInNetwork);
        network.removeContainer(() -> externalStorageWithNonInterfaceConnection);

        regularInterfaceState.setCurrentlyExported(0, A, 10);
        regularInterfaceState.clearRequestedResources();

        // Act
        regularInterface.doWork();

        // Assert
        assertThat(interfaceWithExternalStorageState.getExportedResource(0))
            .usingRecursiveComparison()
            .isEqualTo(new ResourceTemplate(A, NetworkTestFixtures.STORAGE_CHANNEL_TYPE));
        assertThat(interfaceWithExternalStorageState.getExportedAmount(0)).isEqualTo(10);

        assertThat(interfaceWithExternalStorageState2.getExportedResource(0)).isNull();
        assertThat(interfaceWithExternalStorageState2.getExportedAmount(0)).isZero();

        assertThat(regularInterfaceState.getExportedResource(1)).isNull();
        assertThat(regularInterfaceState.getExportedAmount(1)).isZero();

        assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 10)
        );
    }

    // Extractions
    // originating from Interfaces acting as External Storage
    // extracting from other Interfaces acting as External Storage
    // isn't allowed as it would create an extraction loop causing the Interfaces to constantly steal from each other.
    @Test
    void shouldNotAllowExtractionRequestedByAnotherInterfaceIfThatInterfaceIsActingAsExternalStorage(
        @InjectNetworkStorageChannel final StorageChannel networkStorage
    ) {
        // Act & assert
        interfaceWithExternalStorage.doWork();
        externalStorageConnectionToInterface.detectChanges();

        assertThat(interfaceWithExternalStorageState.getExportedResource(0)).isNull();
        assertThat(interfaceWithExternalStorageState.getExportedResource(1))
            .usingRecursiveComparison()
            .isEqualTo(new ResourceTemplate(A, NetworkTestFixtures.STORAGE_CHANNEL_TYPE));
        assertThat(interfaceWithExternalStorageState.getExportedAmount(1)).isEqualTo(10);

        assertThat(interfaceWithExternalStorageState2.getExportedResource(0)).isNull();
        assertThat(interfaceWithExternalStorageState2.getExportedResource(1)).isNull();
        assertThat(interfaceWithExternalStorageState2.getExportedAmount(1)).isZero();

        assertThat(regularStorageInNetwork.getAll()).isEmpty();
        assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 10)
        );

        interfaceWithExternalStorage2.doWork();
        externalStorageConnectionToInterface2.detectChanges();

        assertThat(interfaceWithExternalStorageState.getExportedResource(0)).isNull();
        assertThat(interfaceWithExternalStorageState.getExportedResource(1))
            .usingRecursiveComparison()
            .isEqualTo(new ResourceTemplate(A, NetworkTestFixtures.STORAGE_CHANNEL_TYPE));
        assertThat(interfaceWithExternalStorageState.getExportedAmount(1)).isEqualTo(10);

        assertThat(interfaceWithExternalStorageState2.getExportedResource(0)).isNull();
        assertThat(interfaceWithExternalStorageState2.getExportedResource(1)).isNull();
        assertThat(interfaceWithExternalStorageState2.getExportedAmount(1)).isZero();

        assertThat(regularStorageInNetwork.getAll()).isEmpty();
        assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 10)
        );
    }

    @Test
    void shouldAllowExtractionRequestedByAnotherInterfaceIfThatInterfaceIsNotActingAsExternalStorage(
        @InjectNetworkStorageChannel final StorageChannel networkStorage
    ) {
        // Act & assert
        interfaceWithExternalStorage.doWork();
        externalStorageConnectionToInterface.detectChanges();

        assertThat(interfaceWithExternalStorageState.getExportedResource(0)).isNull();
        assertThat(interfaceWithExternalStorageState.getExportedResource(1))
            .usingRecursiveComparison()
            .isEqualTo(new ResourceTemplate(A, NetworkTestFixtures.STORAGE_CHANNEL_TYPE));
        assertThat(interfaceWithExternalStorageState.getExportedAmount(1)).isEqualTo(10);

        assertThat(regularInterfaceState.getExportedResource(0)).isNull();
        assertThat(regularInterfaceState.getExportedResource(1)).isNull();
        assertThat(regularInterfaceState.getExportedAmount(1)).isZero();

        assertThat(regularStorageInNetwork.getAll()).isEmpty();
        assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 10)
        );

        regularInterface.doWork();
        // update these also to ensure that they don't steal back.
        interfaceWithExternalStorage.doWork();
        externalStorageConnectionToInterface.detectChanges();

        assertThat(interfaceWithExternalStorageState.getExportedResource(0)).isNull();
        assertThat(interfaceWithExternalStorageState.getExportedResource(1)).isNull();
        assertThat(interfaceWithExternalStorageState.getExportedAmount(1)).isZero();

        assertThat(regularInterfaceState.getExportedResource(0)).isNull();
        assertThat(regularInterfaceState.getExportedResource(1)).usingRecursiveComparison().isEqualTo(
            new ResourceTemplate(A, NetworkTestFixtures.STORAGE_CHANNEL_TYPE)
        );
        assertThat(regularInterfaceState.getExportedAmount(1)).isEqualTo(10);

        assertThat(regularStorageInNetwork.getAll()).isEmpty();
        assertThat(networkStorage.getAll()).isEmpty();
    }
}
