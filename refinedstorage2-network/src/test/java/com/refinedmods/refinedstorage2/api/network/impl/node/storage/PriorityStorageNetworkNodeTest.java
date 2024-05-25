package com.refinedmods.refinedstorage2.api.network.impl.node.storage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.impl.node.ProviderImpl;
import com.refinedmods.refinedstorage2.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.api.storage.EmptyActor;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorageImpl;
import com.refinedmods.refinedstorage2.network.test.AddNetworkNode;
import com.refinedmods.refinedstorage2.network.test.InjectNetworkStorageComponent;
import com.refinedmods.refinedstorage2.network.test.NetworkTest;
import com.refinedmods.refinedstorage2.network.test.SetupNetwork;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.refinedmods.refinedstorage2.network.test.fake.FakeResources.A;
import static org.assertj.core.api.Assertions.assertThat;

@NetworkTest
@SetupNetwork
class PriorityStorageNetworkNodeTest {
    @AddNetworkNode
    StorageNetworkNode a;

    @AddNetworkNode
    StorageNetworkNode b;

    ProviderImpl provider;

    @BeforeEach
    void setUp() {
        provider = new ProviderImpl();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldRespectPriority(
        final boolean storageAHasPriority,
        @InjectNetworkStorageComponent final StorageNetworkComponent networkStorage
    ) {
        // Arrange
        final Storage storage1 = new LimitedStorageImpl(100);
        final ProviderImpl provider1 = new ProviderImpl();
        provider1.set(1, storage1);
        a.setProvider(provider1);
        a.setActive(true);

        final Storage storage2 = new LimitedStorageImpl(100);
        final ProviderImpl provider2 = new ProviderImpl();
        provider2.set(1, storage2);
        b.setProvider(provider2);
        b.setActive(true);

        if (storageAHasPriority) {
            a.setPriority(5);
            b.setPriority(2);
        } else {
            a.setPriority(2);
            b.setPriority(5);
        }

        // Act
        networkStorage.insert(A, 1, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        if (storageAHasPriority) {
            assertThat(storage1.getAll()).isNotEmpty();
            assertThat(storage2.getAll()).isEmpty();
        } else {
            assertThat(storage1.getAll()).isEmpty();
            assertThat(storage2.getAll()).isNotEmpty();
        }
    }
}
