package com.refinedmods.refinedstorage.api.network.impl.node.storagetransfer;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.impl.node.ProviderImpl;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.filter.FilterMode;
import com.refinedmods.refinedstorage.api.resource.list.ResourceListImpl;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.EmptyActor;
import com.refinedmods.refinedstorage.api.storage.InMemoryStorageImpl;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.api.storage.limited.LimitedStorageImpl;
import com.refinedmods.refinedstorage.network.test.AddNetworkNode;
import com.refinedmods.refinedstorage.network.test.InjectNetworkStorageComponent;
import com.refinedmods.refinedstorage.network.test.NetworkTest;
import com.refinedmods.refinedstorage.network.test.SetupNetwork;

import java.util.LinkedHashMap;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static com.refinedmods.refinedstorage.network.test.fake.FakeResources.A;
import static com.refinedmods.refinedstorage.network.test.fake.FakeResources.A_ALTERNATIVE;
import static com.refinedmods.refinedstorage.network.test.fake.FakeResources.B;
import static com.refinedmods.refinedstorage.network.test.fake.FakeResources.C;
import static com.refinedmods.refinedstorage.network.test.fake.FakeResources.D;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@NetworkTest
@SetupNetwork
class StorageTransferNetworkNodeTest {
    @AddNetworkNode
    StorageTransferNetworkNode sut;
    StorageTransferListener listener;
    ProviderImpl provider;

    @BeforeEach
    void setUp() {
        provider = new ProviderImpl();
        listener = mock(StorageTransferListener.class);
        sut.setListener(listener);
    }

    @Test
    void shouldNotTransferWithoutNetwork(@InjectNetworkStorageComponent final StorageNetworkComponent networkStorage) {
        // Arrange
        networkStorage.addSource(new InMemoryStorageImpl());

        final Storage source = new InMemoryStorageImpl(new ResourceListImpl(new LinkedHashMap<>()));
        source.insert(D, 5, Action.EXECUTE, EmptyActor.INSTANCE);
        provider.set(0, source);

        sut.setProvider(provider);
        sut.setTransferQuotaProvider(storage -> 20L);

        // Act
        sut.setNetwork(null);
        sut.doWork();

        // Assert
        assertThat(networkStorage.getAll()).isEmpty();
        assertThat(source.getAll()).isNotEmpty();
    }

    @Test
    void shouldNotTransferWhenInactive(@InjectNetworkStorageComponent final StorageNetworkComponent networkStorage) {
        // Arrange
        networkStorage.addSource(new InMemoryStorageImpl());

        final Storage source = new InMemoryStorageImpl(new ResourceListImpl(new LinkedHashMap<>()));
        source.insert(D, 5, Action.EXECUTE, EmptyActor.INSTANCE);
        provider.set(0, source);

        sut.setProvider(provider);
        sut.setTransferQuotaProvider(storage -> 20L);

        // Act
        sut.setActive(false);
        sut.doWork();

        // Assert
        assertThat(networkStorage.getAll()).isEmpty();
        assertThat(source.getAll()).isNotEmpty();
    }

    @ParameterizedTest
    @EnumSource(StorageTransferMode.class)
    void shouldNotTransferWithoutTransferQuotaProvider(
        final StorageTransferMode mode,
        @InjectNetworkStorageComponent final StorageNetworkComponent networkStorage
    ) {
        // Arrange
        networkStorage.addSource(new InMemoryStorageImpl());
        networkStorage.insert(A, 5, Action.EXECUTE, EmptyActor.INSTANCE);

        final Storage source = new InMemoryStorageImpl();
        source.insert(B, 5, Action.EXECUTE, EmptyActor.INSTANCE);
        provider.set(0, source);

        sut.setProvider(provider);
        sut.setMode(mode);

        // Act
        sut.doWork();

        // Assert
        assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 5)
        );
        assertThat(source.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(B, 5)
        );
    }

    @Test
    void shouldInsert(@InjectNetworkStorageComponent final StorageNetworkComponent networkStorage) {
        // Arrange
        networkStorage.addSource(new InMemoryStorageImpl());

        final Storage source0 = new LimitedStorageImpl(1) {
            @Override
            public long extract(final ResourceKey resource,
                                final long amount,
                                final Action action,
                                final Actor actor) {
                return 0;
            }
        };
        source0.insert(A, 1, Action.EXECUTE, EmptyActor.INSTANCE);
        provider.set(0, source0);

        final Storage source1 = new InMemoryStorageImpl(new ResourceListImpl(new LinkedHashMap<>()));
        source1.insert(A, 5, Action.EXECUTE, EmptyActor.INSTANCE);
        source1.insert(B, 5, Action.EXECUTE, EmptyActor.INSTANCE);
        source1.insert(C, 35, Action.EXECUTE, EmptyActor.INSTANCE);
        source1.insert(D, 5, Action.EXECUTE, EmptyActor.INSTANCE);
        provider.set(1, source1);

        final Storage source2 = new InMemoryStorageImpl(new ResourceListImpl(new LinkedHashMap<>()));
        source2.insert(D, 5, Action.EXECUTE, EmptyActor.INSTANCE);
        provider.set(2, source2);

        sut.setProvider(provider);
        sut.setTransferQuotaProvider(storage -> 20L);

        // Act
        sut.doWork();

        // Assert
        assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 5),
            new ResourceAmount(B, 5),
            new ResourceAmount(C, 10)
        );
        assertThat(source0.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 1)
        );
        assertThat(source1.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(C, 25),
            new ResourceAmount(D, 5)
        );
        assertThat(source2.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(D, 5)
        );
        verify(listener, never()).onTransferSuccess(anyInt());
    }

    @Test
    void shouldInsertAllowlist(@InjectNetworkStorageComponent final StorageNetworkComponent networkStorage) {
        // Arrange
        networkStorage.addSource(new InMemoryStorageImpl());

        final Storage source1 = new InMemoryStorageImpl(new ResourceListImpl(new LinkedHashMap<>()));
        source1.insert(A, 5, Action.EXECUTE, EmptyActor.INSTANCE);
        source1.insert(B, 5, Action.EXECUTE, EmptyActor.INSTANCE);
        source1.insert(C, 35, Action.EXECUTE, EmptyActor.INSTANCE);
        source1.insert(D, 5, Action.EXECUTE, EmptyActor.INSTANCE);
        provider.set((sut.getSize() / 2) - 2, source1);

        final Storage source2 = new InMemoryStorageImpl(new ResourceListImpl(new LinkedHashMap<>()));
        source2.insert(D, 5, Action.EXECUTE, EmptyActor.INSTANCE);
        provider.set((sut.getSize() / 2) - 1, source2);

        sut.setProvider(provider);
        sut.setTransferQuotaProvider(storage -> 20L);
        sut.setFilterMode(FilterMode.ALLOW);
        sut.setFilters(Set.of(A, B));

        // Act
        sut.doWork();

        // Assert
        assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 5),
            new ResourceAmount(B, 5)
        );
        assertThat(source1.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(C, 35),
            new ResourceAmount(D, 5)
        );
        assertThat(source2.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(D, 5)
        );
        verify(listener, times(1)).onTransferSuccess((sut.getSize() / 2) - 2);
    }

    @Test
    void shouldInsertBlocklist(@InjectNetworkStorageComponent final StorageNetworkComponent networkStorage) {
        // Arrange
        networkStorage.addSource(new InMemoryStorageImpl());

        final Storage source1 = new InMemoryStorageImpl(new ResourceListImpl(new LinkedHashMap<>()));
        source1.insert(A, 5, Action.EXECUTE, EmptyActor.INSTANCE);
        source1.insert(B, 5, Action.EXECUTE, EmptyActor.INSTANCE);
        source1.insert(C, 35, Action.EXECUTE, EmptyActor.INSTANCE);
        source1.insert(D, 5, Action.EXECUTE, EmptyActor.INSTANCE);
        provider.set(0, source1);

        final Storage source2 = new InMemoryStorageImpl(new ResourceListImpl(new LinkedHashMap<>()));
        source2.insert(D, 5, Action.EXECUTE, EmptyActor.INSTANCE);
        provider.set(1, source2);

        sut.setProvider(provider);
        sut.setTransferQuotaProvider(storage -> 20L);
        sut.setFilterMode(FilterMode.BLOCK);
        sut.setFilters(Set.of(A, B));

        // Act
        sut.doWork();

        // Assert
        assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(C, 20)
        );
        assertThat(source1.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 5),
            new ResourceAmount(B, 5),
            new ResourceAmount(C, 15),
            new ResourceAmount(D, 5)
        );
        assertThat(source2.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(D, 5)
        );
        verify(listener, never()).onTransferSuccess(anyInt());
    }

    @Test
    void shouldNotifyListenerWhenReadyInsertingBecauseStorageWasAlreadyEmpty() {
        // Arrange
        final Storage source = new InMemoryStorageImpl();
        provider.set(0, source);

        sut.setProvider(provider);
        sut.setTransferQuotaProvider(storage -> 15L);

        // Act
        sut.doWork();

        // Assert
        verify(listener, times(1)).onTransferSuccess(0);
    }

    @Test
    void shouldNotifyListenerWhenReadyInsertingAllResources(
        @InjectNetworkStorageComponent final StorageNetworkComponent networkStorage
    ) {
        // Arrange
        networkStorage.addSource(new InMemoryStorageImpl());

        final Storage source = new InMemoryStorageImpl(new ResourceListImpl(new LinkedHashMap<>()));
        source.insert(A, 5, Action.EXECUTE, EmptyActor.INSTANCE);
        source.insert(B, 5, Action.EXECUTE, EmptyActor.INSTANCE);
        source.insert(C, 5, Action.EXECUTE, EmptyActor.INSTANCE);
        provider.set(0, source);

        sut.setProvider(provider);
        sut.setTransferQuotaProvider(storage -> 15L);

        // Act
        sut.doWork();

        // Assert
        assertThat(source.getAll()).isEmpty();
        verify(listener, times(1)).onTransferSuccess(0);
    }

    @Test
    void shouldNotifyListenerWhenReadyInsertingAllResourcesAndUsingFilterButInsertedNothing() {
        // Arrange
        final Storage source1 = new InMemoryStorageImpl(new ResourceListImpl(new LinkedHashMap<>()));
        source1.insert(B, 5, Action.EXECUTE, EmptyActor.INSTANCE);
        source1.insert(C, 5, Action.EXECUTE, EmptyActor.INSTANCE);
        provider.set(0, source1);

        final Storage source2 = new InMemoryStorageImpl(new ResourceListImpl(new LinkedHashMap<>()));
        source2.insert(D, 5, Action.EXECUTE, EmptyActor.INSTANCE);
        provider.set(1, source2);

        sut.setProvider(provider);
        sut.setTransferQuotaProvider(storage -> 15L);
        sut.setFilterMode(FilterMode.ALLOW);
        sut.setFilters(Set.of(A));

        // Act
        sut.doWork();

        // Assert
        assertThat(source1.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(B, 5),
            new ResourceAmount(C, 5)
        );
        assertThat(source2.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(D, 5)
        );
        verify(listener, times(1)).onTransferSuccess(0);
    }

    @Test
    void shouldNotifyListenerWhenReadyInsertingAllResourcesAndUsingFilterButStillInsertedSomething(
        @InjectNetworkStorageComponent final StorageNetworkComponent networkStorage
    ) {
        // Arrange
        networkStorage.addSource(new InMemoryStorageImpl());

        final Storage source1 = new InMemoryStorageImpl(new ResourceListImpl(new LinkedHashMap<>()));
        source1.insert(A, 5, Action.EXECUTE, EmptyActor.INSTANCE);
        source1.insert(B, 5, Action.EXECUTE, EmptyActor.INSTANCE);
        source1.insert(C, 5, Action.EXECUTE, EmptyActor.INSTANCE);
        provider.set(0, source1);

        final Storage source2 = new InMemoryStorageImpl(new ResourceListImpl(new LinkedHashMap<>()));
        source2.insert(D, 5, Action.EXECUTE, EmptyActor.INSTANCE);
        provider.set(1, source2);

        sut.setProvider(provider);
        sut.setTransferQuotaProvider(storage -> 15L);
        sut.setFilterMode(FilterMode.ALLOW);
        sut.setFilters(Set.of(A));

        // Act
        sut.doWork();

        // Assert
        assertThat(source1.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(B, 5),
            new ResourceAmount(C, 5)
        );
        assertThat(source2.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(D, 5)
        );
        verify(listener, times(1)).onTransferSuccess(0);
    }

    @Test
    void shouldNotNotifyListenerWhenReadyInsertingAllResourcesAndNetworkIsFull(
        @InjectNetworkStorageComponent final StorageNetworkComponent networkStorage
    ) {
        // Arrange
        networkStorage.addSource(new LimitedStorageImpl(15));

        final Storage source1 = new InMemoryStorageImpl(new ResourceListImpl(new LinkedHashMap<>()));
        source1.insert(A, 5, Action.EXECUTE, EmptyActor.INSTANCE);
        source1.insert(B, 5, Action.EXECUTE, EmptyActor.INSTANCE);
        source1.insert(C, 5, Action.EXECUTE, EmptyActor.INSTANCE);
        source1.insert(D, 5, Action.EXECUTE, EmptyActor.INSTANCE);
        provider.set(0, source1);

        final Storage source2 = new InMemoryStorageImpl(new ResourceListImpl(new LinkedHashMap<>()));
        source2.insert(A, 5, Action.EXECUTE, EmptyActor.INSTANCE);
        provider.set(1, source2);

        sut.setProvider(provider);
        sut.setTransferQuotaProvider(storage -> 100L);

        // Act
        sut.doWork();
        sut.doWork();

        // Assert
        assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 5),
            new ResourceAmount(B, 5),
            new ResourceAmount(C, 5)
        );
        assertThat(source1.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(D, 5)
        );
        assertThat(source2.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 5)
        );
        verify(listener, never()).onTransferSuccess(anyInt());
    }

    @Test
    void shouldExtract(@InjectNetworkStorageComponent final StorageNetworkComponent networkStorage) {
        // Arrange
        networkStorage.addSource(new InMemoryStorageImpl());
        networkStorage.insert(A, 5, Action.EXECUTE, EmptyActor.INSTANCE);
        networkStorage.insert(B, 5, Action.EXECUTE, EmptyActor.INSTANCE);
        networkStorage.insert(C, 35, Action.EXECUTE, EmptyActor.INSTANCE);
        networkStorage.insert(D, 5, Action.EXECUTE, EmptyActor.INSTANCE);

        final Storage source1 = new LimitedStorageImpl(0);
        provider.set(0, source1);

        final Storage source2 = new InMemoryStorageImpl();
        provider.set(1, source2);

        sut.setProvider(provider);
        sut.setTransferQuotaProvider(storage -> 20L);
        sut.setMode(StorageTransferMode.EXTRACT_FROM_NETWORK);

        // Act
        sut.doWork();

        // Assert
        assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(C, 25),
            new ResourceAmount(D, 5)
        );
        assertThat(source2.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 5),
            new ResourceAmount(B, 5),
            new ResourceAmount(C, 10)
        );
        assertThat(source1.getAll()).isEmpty();
        verify(listener, never()).onTransferSuccess(anyInt());
    }

    @Test
    void shouldExtractAllowlist(@InjectNetworkStorageComponent final StorageNetworkComponent networkStorage) {
        // Arrange
        networkStorage.addSource(new InMemoryStorageImpl());
        networkStorage.insert(A, 5, Action.EXECUTE, EmptyActor.INSTANCE);
        networkStorage.insert(B, 5, Action.EXECUTE, EmptyActor.INSTANCE);
        networkStorage.insert(C, 35, Action.EXECUTE, EmptyActor.INSTANCE);
        networkStorage.insert(D, 5, Action.EXECUTE, EmptyActor.INSTANCE);

        final Storage source1 = new InMemoryStorageImpl();
        provider.set(0, source1);

        final Storage source2 = new InMemoryStorageImpl();
        provider.set(1, source2);

        sut.setProvider(provider);
        sut.setTransferQuotaProvider(storage -> 20L);
        sut.setMode(StorageTransferMode.EXTRACT_FROM_NETWORK);
        sut.setFilterMode(FilterMode.ALLOW);
        sut.setFilters(Set.of(A, B));

        // Act
        sut.doWork();

        // Assert
        assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(C, 35),
            new ResourceAmount(D, 5)
        );
        assertThat(source1.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 5),
            new ResourceAmount(B, 5)
        );
        assertThat(source2.getAll()).isEmpty();
        verify(listener, times(1)).onTransferSuccess(0);
    }

    @Test
    void shouldExtractBlocklist(@InjectNetworkStorageComponent final StorageNetworkComponent networkStorage) {
        // Arrange
        networkStorage.addSource(new InMemoryStorageImpl());
        networkStorage.insert(A, 5, Action.EXECUTE, EmptyActor.INSTANCE);
        networkStorage.insert(B, 5, Action.EXECUTE, EmptyActor.INSTANCE);
        networkStorage.insert(C, 35, Action.EXECUTE, EmptyActor.INSTANCE);
        networkStorage.insert(D, 5, Action.EXECUTE, EmptyActor.INSTANCE);

        final Storage source1 = new InMemoryStorageImpl();
        provider.set(0, source1);

        final Storage source2 = new InMemoryStorageImpl();
        provider.set(1, source2);

        sut.setProvider(provider);
        sut.setTransferQuotaProvider(storage -> 20L);
        sut.setMode(StorageTransferMode.EXTRACT_FROM_NETWORK);
        sut.setFilterMode(FilterMode.BLOCK);
        sut.setFilters(Set.of(A, B));

        // Act
        sut.doWork();

        // Assert
        assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 5),
            new ResourceAmount(B, 5),
            new ResourceAmount(C, 15),
            new ResourceAmount(D, 5)
        );
        assertThat(source1.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(C, 20)
        );
        assertThat(source2.getAll()).isEmpty();
        verify(listener, never()).onTransferSuccess(anyInt());
    }

    @Test
    void shouldNotifyListenerWhenReadyExtractingBecauseStorageWasAlreadyEmpty() {
        // Arrange
        final Storage source = new InMemoryStorageImpl();
        provider.set(0, source);

        sut.setProvider(provider);
        sut.setTransferQuotaProvider(storage -> 15L);
        sut.setMode(StorageTransferMode.EXTRACT_FROM_NETWORK);

        // Act
        sut.doWork();

        // Assert
        verify(listener, times(1)).onTransferSuccess(0);
    }

    @Test
    void shouldNotifyListenerWhenReadyExtractingAllResources(
        @InjectNetworkStorageComponent final StorageNetworkComponent networkStorage
    ) {
        // Arrange
        networkStorage.addSource(new InMemoryStorageImpl());
        networkStorage.insert(A, 5, Action.EXECUTE, EmptyActor.INSTANCE);
        networkStorage.insert(B, 5, Action.EXECUTE, EmptyActor.INSTANCE);
        networkStorage.insert(C, 5, Action.EXECUTE, EmptyActor.INSTANCE);

        final Storage source = new InMemoryStorageImpl();
        provider.set(0, source);

        sut.setProvider(provider);
        sut.setTransferQuotaProvider(storage -> 15L);
        sut.setMode(StorageTransferMode.EXTRACT_FROM_NETWORK);

        // Act
        sut.doWork();

        // Assert
        assertThat(networkStorage.getAll()).isEmpty();
        assertThat(source.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 5),
            new ResourceAmount(B, 5),
            new ResourceAmount(C, 5)
        );
        verify(listener, times(1)).onTransferSuccess(0);
    }

    @Test
    void shouldNotifyListenerWhenReadyExtractingAllResourcesAndUsingFilterButInsertedNothing(
        @InjectNetworkStorageComponent final StorageNetworkComponent networkStorage
    ) {
        // Arrange
        networkStorage.addSource(new InMemoryStorageImpl());
        networkStorage.insert(B, 5, Action.EXECUTE, EmptyActor.INSTANCE);
        networkStorage.insert(C, 5, Action.EXECUTE, EmptyActor.INSTANCE);
        networkStorage.insert(D, 5, Action.EXECUTE, EmptyActor.INSTANCE);

        final Storage source = new InMemoryStorageImpl();
        provider.set(0, source);

        sut.setProvider(provider);
        sut.setTransferQuotaProvider(storage -> 15L);
        sut.setFilterMode(FilterMode.ALLOW);
        sut.setFilters(Set.of(A));
        sut.setMode(StorageTransferMode.EXTRACT_FROM_NETWORK);

        // Act
        sut.doWork();

        // Assert
        assertThat(source.getAll()).isEmpty();
        assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(B, 5),
            new ResourceAmount(C, 5),
            new ResourceAmount(D, 5)
        );
        verify(listener, times(1)).onTransferSuccess(0);
    }

    @Test
    void shouldNotifyListenerWhenReadyExtractingAllResourcesAndUsingFilterButStillExtractedSomething(
        @InjectNetworkStorageComponent final StorageNetworkComponent networkStorage
    ) {
        // Arrange
        networkStorage.addSource(new InMemoryStorageImpl(new ResourceListImpl(new LinkedHashMap<>())));
        networkStorage.insert(A, 5, Action.EXECUTE, EmptyActor.INSTANCE);
        networkStorage.insert(B, 5, Action.EXECUTE, EmptyActor.INSTANCE);
        networkStorage.insert(C, 5, Action.EXECUTE, EmptyActor.INSTANCE);

        final Storage source = new InMemoryStorageImpl(new ResourceListImpl(new LinkedHashMap<>()));
        provider.set(0, source);

        sut.setProvider(provider);
        sut.setTransferQuotaProvider(storage -> 15L);
        sut.setFilterMode(FilterMode.ALLOW);
        sut.setFilters(Set.of(A));
        sut.setMode(StorageTransferMode.EXTRACT_FROM_NETWORK);

        // Act
        sut.doWork();

        // Assert
        assertThat(source.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 5)
        );
        assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(B, 5),
            new ResourceAmount(C, 5)
        );
        verify(listener, times(1)).onTransferSuccess(0);
    }

    @Test
    void shouldNotifyListenerWhenExtractingAllResourcesAndReachingCapacity(
        @InjectNetworkStorageComponent final StorageNetworkComponent networkStorage
    ) {
        // Arrange
        networkStorage.addSource(new InMemoryStorageImpl());
        networkStorage.insert(A, 5, Action.EXECUTE, EmptyActor.INSTANCE);
        networkStorage.insert(B, 5, Action.EXECUTE, EmptyActor.INSTANCE);
        networkStorage.insert(C, 5, Action.EXECUTE, EmptyActor.INSTANCE);

        final Storage source = new LimitedStorageImpl(10);
        provider.set(0, source);

        sut.setProvider(provider);
        sut.setTransferQuotaProvider(storage -> 100L);
        sut.setMode(StorageTransferMode.EXTRACT_FROM_NETWORK);

        // Act
        sut.doWork();

        // Assert
        assertThat(source.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 5),
            new ResourceAmount(B, 5)
        );
        assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(C, 5)
        );
        verify(listener, times(1)).onTransferSuccess(0);
    }

    @Test
    void shouldRespectNormalizer(
        @InjectNetworkStorageComponent final StorageNetworkComponent networkStorage
    ) {
        // Arrange
        networkStorage.addSource(new InMemoryStorageImpl());

        final Storage source = new InMemoryStorageImpl(new ResourceListImpl(new LinkedHashMap<>()));
        source.insert(A, 5, Action.EXECUTE, EmptyActor.INSTANCE);
        source.insert(A_ALTERNATIVE, 5, Action.EXECUTE, EmptyActor.INSTANCE);
        source.insert(B, 5, Action.EXECUTE, EmptyActor.INSTANCE);
        source.insert(D, 5, Action.EXECUTE, EmptyActor.INSTANCE);
        provider.set(0, source);

        sut.setProvider(provider);
        sut.setTransferQuotaProvider(storage -> 20L);
        sut.setFilterMode(FilterMode.ALLOW);
        sut.setFilters(Set.of(A));
        sut.setNormalizer(resource -> resource == A || resource == A_ALTERNATIVE ? A : resource);

        // Act
        sut.doWork();

        // Assert
        assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 5),
            new ResourceAmount(A_ALTERNATIVE, 5)
        );
        assertThat(source.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(B, 5),
            new ResourceAmount(D, 5)
        );
        verify(listener, times(1)).onTransferSuccess(0);
    }

    @Test
    void shouldNotTransferAtIndexHigherThanHalf(
        @InjectNetworkStorageComponent final StorageNetworkComponent networkStorage
    ) {
        // Arrange
        networkStorage.addSource(new InMemoryStorageImpl());

        final Storage source = new InMemoryStorageImpl();
        source.insert(A, 5, Action.EXECUTE, EmptyActor.INSTANCE);
        provider.set(sut.getSize() / 2, source);

        sut.setProvider(provider);
        sut.setTransferQuotaProvider(storage -> 20L);

        // Act
        sut.doWork();

        // Assert
        assertThat(networkStorage.getAll()).isEmpty();
        assertThat(source.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 5)
        );
        verify(listener, never()).onTransferSuccess(1);
    }
}
