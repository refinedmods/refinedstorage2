package com.refinedmods.refinedstorage2.platform.common.internal.storage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.EmptyActor;
import com.refinedmods.refinedstorage2.api.storage.InMemoryStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.StorageInfo;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.tracked.InMemoryTrackedStorageRepository;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorage;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorageImpl;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.storage.PlayerActor;
import com.refinedmods.refinedstorage2.platform.common.PlatformTestFixtures;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.type.ItemStorageType;
import com.refinedmods.refinedstorage2.platform.test.SetupMinecraft;

import java.util.Optional;
import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static com.refinedmods.refinedstorage2.platform.test.TagHelper.createDummyTag;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SetupMinecraft
class StorageRepositoryImplTest {
    StorageRepositoryImpl sut;

    @BeforeEach
    void setUp() {
        sut = new StorageRepositoryImpl(PlatformTestFixtures.STORAGE_TYPE_REGISTRY);
    }

    private PlatformStorage<ItemResource> createSerializableStorage(final Storage<ItemResource> storage) {
        if (storage instanceof LimitedStorageImpl<ItemResource> limitedStorage) {
            return new LimitedPlatformStorage<>(
                limitedStorage,
                ItemStorageType.INSTANCE,
                new InMemoryTrackedStorageRepository<>(),
                () -> {
                }
            );
        }
        return new PlatformStorage<>(
            storage,
            ItemStorageType.INSTANCE,
            new InMemoryTrackedStorageRepository<>(),
            () -> {
            }
        );
    }

    @Test
    void testInitialState() {
        // Assert
        assertThat(sut.isDirty()).isFalse();
    }

    @Test
    void shouldNotRetrieveNonExistentStorage() {
        // Assert
        assertThat(sut.get(UUID.randomUUID())).isEmpty();
    }

    @Test
    void shouldBeAbleToSetAndRetrieveStorage() {
        // Arrange
        final UUID id = UUID.randomUUID();
        final Storage<ItemResource> storage = createSerializableStorage(new InMemoryStorageImpl<>());
        storage.insert(new ItemResource(Items.DIRT, null), 10, Action.EXECUTE, EmptyActor.INSTANCE);

        // Act
        sut.set(id, storage);

        // Assert
        assertThat(sut.<ItemResource>get(id)).containsSame(storage);
        assertThat(sut.isDirty()).isTrue();
        assertThat(sut.getInfo(id)).usingRecursiveComparison().isEqualTo(new StorageInfo(10, 0));
    }

    @Test
    void shouldNotBeAbleToSetStorageWithExistingId() {
        // Arrange
        final UUID id = UUID.randomUUID();
        sut.set(id, createSerializableStorage(new InMemoryStorageImpl<>()));

        // Act
        final Executable action = () -> sut.set(id, createSerializableStorage(new InMemoryStorageImpl<>()));

        // Assert
        assertThrows(IllegalArgumentException.class, action);
    }

    @Test
    void shouldNotBeAbleToSetUnserializableStorage() {
        // Arrange
        final UUID id = UUID.randomUUID();
        final InMemoryStorageImpl<String> storage = new InMemoryStorageImpl<>();

        // Act & assert
        assertThrows(IllegalArgumentException.class, () -> sut.set(id, storage));
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void shouldNotBeAbleToSetWithInvalidId() {
        // Arrange
        final Storage<ItemResource> storage = createSerializableStorage(new InMemoryStorageImpl<>());

        // Act & assert
        assertThrows(NullPointerException.class, () -> sut.set(null, storage));
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void shouldNotBeAbleToSetWithInvalidStorage() {
        // Arrange
        final UUID id = UUID.randomUUID();

        // Act & assert
        assertThrows(NullPointerException.class, () -> sut.set(id, null));
    }

    @Test
    void shouldRemoveIfEmpty() {
        // Arrange
        final UUID id = UUID.randomUUID();
        final Storage<ItemResource> storage = createSerializableStorage(new InMemoryStorageImpl<>());
        sut.set(id, storage);
        sut.setDirty(false);

        // Act
        final Optional<Storage<ItemResource>> result = sut.removeIfEmpty(id);

        // Assert
        assertThat(result).get().isEqualTo(storage);
        assertThat(sut.isDirty()).isTrue();
        assertThat(sut.get(id)).isEmpty();
    }

    @Test
    void shouldNotRemoveIfEmptyIfNotEmpty() {
        // Arrange
        final UUID id = UUID.randomUUID();
        final Storage<ItemResource> storage = createSerializableStorage(new InMemoryStorageImpl<>());
        storage.insert(new ItemResource(Items.DIRT, null), 10, Action.EXECUTE, EmptyActor.INSTANCE);
        sut.set(id, storage);
        sut.setDirty(false);

        // Act
        final Optional<Storage<String>> result = sut.removeIfEmpty(id);

        // Assert
        assertThat(result).isEmpty();
        assertThat(sut.isDirty()).isFalse();
        assertThat(sut.get(id)).isPresent();
    }

    @Test
    void shouldNotRemoveIfEmptyIfNotExists() {
        // Act
        final Optional<Storage<String>> disassembled = sut.removeIfEmpty(UUID.randomUUID());

        // Assert
        assertThat(disassembled).isEmpty();
        assertThat(sut.isDirty()).isFalse();
    }

    @Test
    void shouldBeDirtyWhenMarkedAsChanged() {
        // Act
        sut.markAsChanged();

        // Assert
        assertThat(sut.isDirty()).isTrue();
    }

    @Test
    void shouldRetrieveInfoFromLimitedStorage() {
        // Arrange
        final UUID id = UUID.randomUUID();
        final Storage<ItemResource> storage = createSerializableStorage(new LimitedStorageImpl<>(10));
        storage.insert(new ItemResource(Items.DIRT, null), 5, Action.EXECUTE, EmptyActor.INSTANCE);

        // Act
        sut.set(id, storage);

        // Assert
        final StorageInfo info = sut.getInfo(id);

        assertThat(info.capacity()).isEqualTo(10);
        assertThat(info.stored()).isEqualTo(5);
    }

    @Test
    void shouldRetrieveInfoFromRegularStorage() {
        // Arrange
        final UUID id = UUID.randomUUID();
        final Storage<ItemResource> storage = createSerializableStorage(new InMemoryStorageImpl<>());
        storage.insert(new ItemResource(Items.DIRT, null), 5, Action.EXECUTE, EmptyActor.INSTANCE);

        // Act
        sut.set(id, storage);

        // Assert
        final StorageInfo info = sut.getInfo(id);

        assertThat(info.capacity()).isZero();
        assertThat(info.stored()).isEqualTo(5);
    }

    @Test
    void shouldRetrieveInfoFromNonExistentStorage() {
        // Act
        final StorageInfo info = sut.getInfo(UUID.randomUUID());

        // Assert
        assertThat(info.capacity()).isZero();
        assertThat(info.stored()).isZero();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void shouldSerializeAndDeserialize() {
        // Arrange
        final InMemoryTrackedStorageRepository<ItemResource> repository = new InMemoryTrackedStorageRepository<>();
        final PlatformStorage<ItemResource> a = new PlatformStorage<>(
            new TrackedStorageImpl<>(new InMemoryStorageImpl<>(), repository, () -> 123L),
            ItemStorageType.INSTANCE,
            repository,
            sut::markAsChanged
        );
        final PlatformStorage<ItemResource> b = new LimitedPlatformStorage<>(
            new LimitedStorageImpl<>(new InMemoryStorageImpl<>(), 100),
            ItemStorageType.INSTANCE,
            new InMemoryTrackedStorageRepository<>(),
            sut::markAsChanged
        );

        final UUID aId = UUID.randomUUID();
        final UUID bId = UUID.randomUUID();

        sut.set(aId, a);
        sut.set(bId, b);

        a.insert(new ItemResource(Items.DIRT, createDummyTag()), 10, Action.EXECUTE, new PlayerActor("A"));
        b.insert(new ItemResource(Items.GLASS, null), 20, Action.EXECUTE, EmptyActor.INSTANCE);

        // Act
        final CompoundTag serialized = sut.save(new CompoundTag());
        sut = new StorageRepositoryImpl(PlatformTestFixtures.STORAGE_TYPE_REGISTRY);
        sut.read(serialized);

        // Assert
        assertThat(sut.isDirty()).isFalse();
        assertThat(sut.get(aId)).isPresent();
        assertThat(sut.get(bId)).isPresent();
        assertThat(sut.get(aId))
            .get()
            .isInstanceOf(PlatformStorage.class);
        assertThat(sut.get(aId).get().getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>(new ItemResource(Items.DIRT, createDummyTag()), 10)
        );
        assertThat(((TrackedStorage) sut.get(aId).get()).findTrackedResourceByActorType(
            new ItemResource(Items.DIRT, createDummyTag()), PlayerActor.class))
            .get()
            .usingRecursiveComparison()
            .isEqualTo(new TrackedResource("A", 123L));
        assertThat(sut.get(bId)).get().isInstanceOf(LimitedPlatformStorage.class);
        assertThat(((LimitedPlatformStorage) sut.get(bId).get()).getCapacity()).isEqualTo(100);
        assertThat(sut.get(bId).get().getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>(new ItemResource(Items.GLASS, null), 20)
        );
    }
}
