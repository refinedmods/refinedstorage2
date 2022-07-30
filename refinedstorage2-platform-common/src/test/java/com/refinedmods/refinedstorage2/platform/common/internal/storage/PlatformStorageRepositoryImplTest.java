package com.refinedmods.refinedstorage2.platform.common.internal.storage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.EmptyActor;
import com.refinedmods.refinedstorage2.api.storage.InMemoryStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.StorageInfo;
import com.refinedmods.refinedstorage2.api.storage.StorageRepositoryImpl;
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

import static com.refinedmods.refinedstorage2.platform.test.TagHelper.createDummyTag;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SetupMinecraft
class PlatformStorageRepositoryImplTest {
    StorageRepositoryImpl delegate;
    PlatformStorageRepositoryImpl sut;

    @BeforeEach
    void setUp() {
        delegate = new StorageRepositoryImpl();
        sut = new PlatformStorageRepositoryImpl(delegate, PlatformTestFixtures.STORAGE_TYPE_REGISTRY);
    }

    @Test
    void testInitialState() {
        // Assert
        assertThat(sut.isDirty()).isFalse();
    }

    @Test
    void shouldSetStorage() {
        // Arrange
        final UUID id = UUID.randomUUID();
        final Storage<ItemResource> storage = new PlatformStorage<>(
            new InMemoryStorageImpl<>(),
            ItemStorageType.INSTANCE,
            new InMemoryTrackedStorageRepository<>(),
            () -> {
            }
        );
        storage.insert(new ItemResource(Items.DIRT, null), 10, Action.EXECUTE, EmptyActor.INSTANCE);

        // Act
        sut.set(id, storage);

        // Assert
        assertThat(sut.<ItemResource>get(id)).containsSame(storage);
        assertThat(sut.isDirty()).isTrue();
        assertThat(sut.getInfo(id)).usingRecursiveComparison().isEqualTo(new StorageInfo(10, 0));
    }

    @Test
    void shouldDisassemble() {
        // Arrange
        final UUID id = UUID.randomUUID();
        final Storage<ItemResource> storage = new PlatformStorage<>(
            new InMemoryStorageImpl<>(),
            ItemStorageType.INSTANCE,
            new InMemoryTrackedStorageRepository<>(),
            () -> {
            }
        );
        sut.set(id, storage);
        sut.setDirty(false);

        // Act
        final Optional<Storage<ItemResource>> result = sut.disassemble(id);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(sut.isDirty()).isTrue();
    }

    @Test
    void shouldNotDisassembleWhenNotPossible() {
        // Arrange
        final UUID id = UUID.randomUUID();
        final Storage<ItemResource> storage = new PlatformStorage<>(
            new InMemoryStorageImpl<>(),
            ItemStorageType.INSTANCE,
            new InMemoryTrackedStorageRepository<>(),
            () -> {
            }
        );
        storage.insert(new ItemResource(Items.DIRT, null), 10, Action.EXECUTE, EmptyActor.INSTANCE);
        sut.set(id, storage);
        sut.setDirty(false);

        // Act
        final Optional<Storage<String>> result = sut.disassemble(id);

        // Assert
        assertThat(result).isEmpty();
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
    void shouldNotBeAbleToSerializeUnserializableStorage() {
        // Arrange
        final UUID id = UUID.randomUUID();
        final InMemoryStorageImpl<String> storage = new InMemoryStorageImpl<>();

        // Act & assert
        assertThrows(IllegalArgumentException.class, () -> sut.set(id, storage));
    }

    @Test
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
        final InMemoryStorageImpl<ItemResource> c = new InMemoryStorageImpl<>();

        final UUID aId = UUID.randomUUID();
        final UUID bId = UUID.randomUUID();
        final UUID cId = UUID.randomUUID();

        sut.set(aId, a);
        sut.set(bId, b);
        delegate.set(cId, c); // Set through delegate to bypass serializable checks

        a.insert(new ItemResource(Items.DIRT, createDummyTag()), 10, Action.EXECUTE, new PlayerActor("A"));
        b.insert(new ItemResource(Items.GLASS, null), 20, Action.EXECUTE, EmptyActor.INSTANCE);

        // Act
        final CompoundTag serialized = sut.save(new CompoundTag());
        sut =
            new PlatformStorageRepositoryImpl(new StorageRepositoryImpl(), PlatformTestFixtures.STORAGE_TYPE_REGISTRY);
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
        assertThat(sut.get(cId)).isEmpty();
    }
}
