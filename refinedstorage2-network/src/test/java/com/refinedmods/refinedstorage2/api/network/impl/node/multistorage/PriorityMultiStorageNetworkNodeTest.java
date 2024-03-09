package com.refinedmods.refinedstorage2.api.network.impl.node.multistorage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.storage.EmptyActor;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorageImpl;
import com.refinedmods.refinedstorage2.network.test.AddNetworkNode;
import com.refinedmods.refinedstorage2.network.test.InjectNetworkStorageChannel;
import com.refinedmods.refinedstorage2.network.test.NetworkTest;
import com.refinedmods.refinedstorage2.network.test.SetupNetwork;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.refinedmods.refinedstorage2.network.test.TestResource.A;
import static org.assertj.core.api.Assertions.assertThat;

@NetworkTest
@SetupNetwork
class PriorityMultiStorageNetworkNodeTest {
    @AddNetworkNode
    MultiStorageNetworkNode a;

    @AddNetworkNode
    MultiStorageNetworkNode b;

    MultiStorageProviderImpl provider;

    @BeforeEach
    void setUp() {
        provider = new MultiStorageProviderImpl();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldRespectPriority(
        final boolean multiStorageAHasPriority,
        @InjectNetworkStorageChannel final StorageChannel networkStorage
    ) {
        // Arrange
        final Storage storage1 = new LimitedStorageImpl(100);
        final MultiStorageProviderImpl provider1 = new MultiStorageProviderImpl();
        provider1.set(1, storage1);
        a.setProvider(provider1);
        a.setActive(true);

        final Storage storage2 = new LimitedStorageImpl(100);
        final MultiStorageProviderImpl provider2 = new MultiStorageProviderImpl();
        provider2.set(1, storage2);
        b.setProvider(provider2);
        b.setActive(true);

        if (multiStorageAHasPriority) {
            a.setPriority(5);
            b.setPriority(2);
        } else {
            a.setPriority(2);
            b.setPriority(5);
        }

        // Act
        networkStorage.insert(A, 1, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        if (multiStorageAHasPriority) {
            assertThat(storage1.getAll()).isNotEmpty();
            assertThat(storage2.getAll()).isEmpty();
        } else {
            assertThat(storage1.getAll()).isEmpty();
            assertThat(storage2.getAll()).isNotEmpty();
        }
    }
}
