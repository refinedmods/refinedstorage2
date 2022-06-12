package com.refinedmods.refinedstorage2.platform.apiimpl.storage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.EmptySource;
import com.refinedmods.refinedstorage2.api.storage.InMemoryStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.StorageInfo;
import com.refinedmods.refinedstorage2.api.storage.StorageRepositoryImpl;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.tracked.InMemoryTrackedStorageRepository;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorage;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorageImpl;
import com.refinedmods.refinedstorage2.platform.PlatformTestFixtures;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.storage.PlayerSource;
import com.refinedmods.refinedstorage2.platform.apiimpl.storage.type.ItemStorageType;
import com.refinedmods.refinedstorage2.platform.test.SetupMinecraft;
import com.refinedmods.refinedstorage2.test.Rs2Test;

import java.util.Optional;
import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Rs2Test
@SetupMinecraft
class PlatformStorageRepositoryImplTest {
    PlatformStorageRepositoryImpl sut;

    @BeforeEach
    void setUp() {
        StorageRepositoryImpl delegate = new StorageRepositoryImpl();
        sut = new PlatformStorageRepositoryImpl(delegate, PlatformTestFixtures.STORAGE_TYPE_REGISTRY);
    }

    @Test
    void Test_initial_state() {
        // Assert
        assertThat(sut.isDirty()).isFalse();
    }

    @Test
    void Test_setting_storage() {
        // Arrange
        UUID id = UUID.randomUUID();
        Storage<String> storage = new InMemoryStorageImpl<>();
        storage.insert("A", 10, Action.EXECUTE, EmptySource.INSTANCE);

        // Act
        sut.set(id, storage);

        // Assert
        assertThat(sut.get(id)).containsSame((Storage) storage);
        assertThat(sut.isDirty()).isTrue();
        assertThat(sut.getInfo(id)).usingRecursiveComparison().isEqualTo(new StorageInfo(10, 0));
    }

    @Test
    void Test_disassembling() {
        // Arrange
        UUID id = UUID.randomUUID();
        Storage<String> storage = new InMemoryStorageImpl<>();
        sut.set(id, storage);
        sut.setDirty(false);

        // Act
        Optional<Storage<String>> result = sut.disassemble(id);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(sut.isDirty()).isTrue();
    }

    @Test
    void Test_disassembling_when_not_possible() {
        // Arrange
        UUID id = UUID.randomUUID();
        Storage<String> storage = new InMemoryStorageImpl<>();
        storage.insert("A", 10, Action.EXECUTE, EmptySource.INSTANCE);
        sut.set(id, storage);
        sut.setDirty(false);

        // Act
        Optional<Storage<String>> result = sut.disassemble(id);

        // Assert
        assertThat(result).isEmpty();
        assertThat(sut.isDirty()).isFalse();
    }

    @Test
    void Test_marking_as_changed() {
        // Act
        sut.markAsChanged();

        // Assert
        assertThat(sut.isDirty()).isTrue();
    }

    @Test
    void Test_serializing_and_deserializing() {
        // Arrange
        InMemoryTrackedStorageRepository<ItemResource> repository = new InMemoryTrackedStorageRepository<>();
        PlatformStorage<ItemResource> a = new PlatformStorage<>(new TrackedStorageImpl<>(new InMemoryStorageImpl<>(), repository, () -> 123L), ItemStorageType.INSTANCE, repository, sut::markAsChanged);
        PlatformStorage<ItemResource> b = new LimitedPlatformStorage<>(new LimitedStorageImpl<>(new InMemoryStorageImpl<>(), 100), ItemStorageType.INSTANCE, new InMemoryTrackedStorageRepository<>(), sut::markAsChanged);
        InMemoryStorageImpl<ItemResource> c = new InMemoryStorageImpl<>();

        UUID aId = UUID.randomUUID();
        UUID bId = UUID.randomUUID();
        UUID cId = UUID.randomUUID();

        sut.set(aId, a);
        sut.set(bId, b);
        sut.set(cId, c);

        CompoundTag tag = new CompoundTag();
        tag.putString("dummy", "tag");
        a.insert(new ItemResource(Items.DIRT, tag), 10, Action.EXECUTE, new PlayerSource("A"));

        b.insert(new ItemResource(Items.GLASS, null), 20, Action.EXECUTE, EmptySource.INSTANCE);

        // Act
        CompoundTag serialized = sut.save(new CompoundTag());
        sut = new PlatformStorageRepositoryImpl(new StorageRepositoryImpl(), PlatformTestFixtures.STORAGE_TYPE_REGISTRY);
        sut.read(serialized);

        // Assert
        assertThat(sut.get(aId)).isPresent();
        assertThat(sut.get(bId)).isPresent();
        assertThat(sut.get(aId).get()).isInstanceOf(PlatformStorage.class);
        assertThat(sut.get(aId).get().getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount<>(new ItemResource(Items.DIRT, tag), 10)
        );
        assertThat(((TrackedStorage) sut.get(aId).get()).findTrackedResourceBySourceType(new ItemResource(Items.DIRT, tag), PlayerSource.class))
                .get()
                .usingRecursiveComparison()
                .isEqualTo(new TrackedResource("A", 123L));
        assertThat(sut.get(bId).get()).isInstanceOf(LimitedPlatformStorage.class);
        assertThat(((LimitedPlatformStorage) sut.get(bId).get()).getCapacity()).isEqualTo(100);
        assertThat(sut.get(bId).get().getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount<>(new ItemResource(Items.GLASS, null), 20)
        );
        assertThat(sut.get(cId)).isEmpty();
    }
}
