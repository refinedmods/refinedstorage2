package com.refinedmods.refinedstorage2.api.network.impl.component;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.component.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.impl.NetworkImpl;
import com.refinedmods.refinedstorage2.api.network.impl.node.storage.StorageNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceListImpl;
import com.refinedmods.refinedstorage2.api.storage.EmptyActor;
import com.refinedmods.refinedstorage2.api.storage.TrackedResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorageImpl;
import com.refinedmods.refinedstorage2.network.test.NetworkTestFixtures;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage2.network.test.TestResource.A;
import static com.refinedmods.refinedstorage2.network.test.TestResource.B;
import static org.assertj.core.api.Assertions.assertThat;

class StorageNetworkComponentImplTest {
    private StorageNetworkComponent sut;

    private StorageNetworkNode storage1;
    private NetworkNodeContainer storage1Container;

    private StorageNetworkNode storage2;
    private NetworkNodeContainer storage2Container;

    @BeforeEach
    void setUp() {
        sut = new StorageNetworkComponentImpl(new ResourceListImpl());

        storage1 = new StorageNetworkNode(0);
        storage1.setNetwork(new NetworkImpl(NetworkTestFixtures.NETWORK_COMPONENT_MAP_FACTORY));
        storage1.setStorage(new LimitedStorageImpl(100));
        storage1.setActive(true);
        storage1Container = () -> storage1;

        storage2 = new StorageNetworkNode(0);
        storage2.setNetwork(new NetworkImpl(NetworkTestFixtures.NETWORK_COMPONENT_MAP_FACTORY));
        storage2.setStorage(new LimitedStorageImpl(100));
        storage2.setActive(true);
        storage2Container = () -> storage2;
    }

    @Test
    void testInitialState() {
        // Act
        final Collection<ResourceAmount> resources = sut.getAll();

        // Assert
        assertThat(resources).isEmpty();
    }

    @Test
    void shouldAddStorageSourceContainer() {
        // Act
        final long insertedPre = sut.insert(A, 10, Action.EXECUTE, EmptyActor.INSTANCE);
        sut.onContainerAdded(storage1Container);
        final long insertedPost = sut.insert(A, 10, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        assertThat(insertedPre).isZero();
        assertThat(insertedPost).isEqualTo(10);
        assertThat(sut.getAll()).isNotEmpty();
    }

    @Test
    void shouldRemoveStorageSourceContainer() {
        // Arrange
        sut.onContainerAdded(storage1Container);
        sut.onContainerAdded(storage2Container);

        // Ensure that we fill our 2 containers.
        sut.insert(A, 200, Action.EXECUTE, EmptyActor.INSTANCE);

        // Act
        final Collection<ResourceAmount> resourcesPre = new HashSet<>(sut.getAll());
        sut.onContainerRemoved(storage1Container);
        sut.onContainerRemoved(storage2Container);
        final Collection<ResourceAmount> resourcesPost = sut.getAll();

        // Assert
        assertThat(resourcesPre).isNotEmpty();
        assertThat(resourcesPost).isEmpty();
    }

    @Test
    void testHasSource() {
        // Arrange
        sut.onContainerAdded(storage1Container);

        // Act
        final boolean found = sut.hasSource(s -> s == storage1.getStorage());
        final boolean found2 = sut.hasSource(s -> s == storage2.getStorage());

        // Assert
        assertThat(found).isTrue();
        assertThat(found2).isFalse();
    }

    @Test
    void shouldRetrieveResources() {
        // Arrange
        sut.onContainerRemoved(storage1Container);
        sut.onContainerRemoved(storage2Container);
        sut.addSource(new TrackedStorageImpl(new LimitedStorageImpl(1000), () -> 2L));
        sut.insert(A, 100, Action.EXECUTE, EmptyActor.INSTANCE);
        sut.insert(B, 200, Action.EXECUTE, EmptyActor.INSTANCE);

        // Act
        final List<TrackedResourceAmount> resources = sut.getResources(EmptyActor.class);

        // Assert
        assertThat(resources).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new TrackedResourceAmount(
                new ResourceAmount(A, 100),
                new TrackedResource("Empty", 2L)
            ),
            new TrackedResourceAmount(
                new ResourceAmount(B, 200),
                new TrackedResource("Empty", 2L)
            )
        );
    }
}
