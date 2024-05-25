package com.refinedmods.refinedstorage2.api.network.impl.node.storagetransfer;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.impl.node.ProviderImpl;
import com.refinedmods.refinedstorage2.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.api.storage.EmptyActor;
import com.refinedmods.refinedstorage2.api.storage.InMemoryStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.network.test.AddNetworkNode;
import com.refinedmods.refinedstorage2.network.test.InjectNetworkStorageComponent;
import com.refinedmods.refinedstorage2.network.test.NetworkTest;
import com.refinedmods.refinedstorage2.network.test.SetupNetwork;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage2.network.test.fake.FakeResources.A;
import static com.refinedmods.refinedstorage2.network.test.fake.FakeResources.B;
import static com.refinedmods.refinedstorage2.network.test.fake.FakeResources.C;
import static com.refinedmods.refinedstorage2.network.test.fake.FakeResources.D;

@NetworkTest
@SetupNetwork
class StorageTransferNetworkNodeTest {
    @AddNetworkNode
    StorageTransferNetworkNode sut;
    ProviderImpl provider;

    @BeforeEach
    void setUp() {
        provider = new ProviderImpl();
    }

    @Test
    void shouldInsert(
        @InjectNetworkStorageComponent final StorageNetworkComponent networkStorage
    ) {
        // Arrange
        networkStorage.addSource(new InMemoryStorageImpl());

        final Storage source = new InMemoryStorageImpl();
        source.insert(A, 5, Action.EXECUTE, EmptyActor.INSTANCE);
        source.insert(B, 5, Action.EXECUTE, EmptyActor.INSTANCE);
        source.insert(C, 15, Action.EXECUTE, EmptyActor.INSTANCE);
        source.insert(D, 15, Action.EXECUTE, EmptyActor.INSTANCE);
        provider.set(0, source);
        sut.setProvider(provider);
        sut.setTransferQuotaProvider(storage -> 20L);

        // Act
        sut.doWork();

        // Assert
    }
}
