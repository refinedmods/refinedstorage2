package com.refinedmods.refinedstorage2.platform.common.storage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.EmptyActor;
import com.refinedmods.refinedstorage2.api.storage.InMemoryStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorage;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.tracked.InMemoryTrackedStorageRepository;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorage;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorageImpl;
import com.refinedmods.refinedstorage2.platform.api.storage.PlayerActor;
import com.refinedmods.refinedstorage2.platform.api.storage.StorageType;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.SimpleListener;
import com.refinedmods.refinedstorage2.platform.test.SetupMinecraft;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage2.platform.test.TagHelper.createDummyTag;
import static org.assertj.core.api.Assertions.assertThat;

@SetupMinecraft
class ItemStorageTypeTest {
    StorageType sut = StorageTypes.ITEM;
    SimpleListener listener;

    @BeforeEach
    void setUp() {
        listener = new SimpleListener();
    }

    @Test
    void shouldSerializeAndDeserializeRegularStorage() {
        // Arrange
        final InMemoryTrackedStorageRepository tracker = new InMemoryTrackedStorageRepository();
        final Storage storage = new PlatformStorage(
            new TrackedStorageImpl(new InMemoryStorageImpl(), tracker, () -> 123L),
            StorageTypes.ITEM,
            tracker,
            () -> {
            }
        );

        storage.insert(new ItemResource(Items.DIRT, createDummyTag()), 10, Action.EXECUTE, new PlayerActor("A"));
        storage.insert(new ItemResource(Items.GLASS, null), 15, Action.EXECUTE, EmptyActor.INSTANCE);

        // Act
        final CompoundTag serialized = sut.toTag(storage);
        final Storage deserialized = sut.fromTag(serialized, listener);

        // Assert
        assertThat(listener.isChanged()).isFalse();
        assertThat(deserialized).isInstanceOf(PlatformStorage.class);
        assertThat(deserialized.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(new ItemResource(Items.DIRT, createDummyTag()), 10),
            new ResourceAmount(new ItemResource(Items.GLASS, null), 15)
        );
        assertThat(((TrackedStorage) deserialized).findTrackedResourceByActorType(
            new ItemResource(Items.DIRT, createDummyTag()),
            PlayerActor.class
        )).get().usingRecursiveComparison().isEqualTo(new TrackedResource("A", 123));
        assertThat(((TrackedStorage) deserialized).findTrackedResourceByActorType(
            new ItemResource(Items.GLASS, null),
            PlayerActor.class
        )).isEmpty();
    }

    @Test
    void shouldCallListenerOnDeserializedStorage() {
        // Arrange
        final InMemoryTrackedStorageRepository tracker = new InMemoryTrackedStorageRepository();
        final Storage storage = new PlatformStorage(
            new TrackedStorageImpl(new InMemoryStorageImpl(), tracker, () -> 123L),
            StorageTypes.ITEM,
            tracker,
            () -> {
            }
        );
        storage.insert(new ItemResource(Items.GLASS, null), 15, Action.EXECUTE, EmptyActor.INSTANCE);

        final CompoundTag serialized = sut.toTag(storage);
        final Storage deserialized = sut.fromTag(serialized, listener);

        // Act
        final boolean preInsert = listener.isChanged();
        deserialized.insert(new ItemResource(Items.GLASS, null), 15, Action.EXECUTE, EmptyActor.INSTANCE);
        final boolean postInsert = listener.isChanged();

        // Assert
        assertThat(preInsert).isFalse();
        assertThat(postInsert).isTrue();
    }

    @Test
    void shouldSerializeLimitedStorage() {
        // Arrange
        final InMemoryTrackedStorageRepository tracker = new InMemoryTrackedStorageRepository();
        final Storage storage = new LimitedPlatformStorage(
            new LimitedStorageImpl(new TrackedStorageImpl(new InMemoryStorageImpl(), tracker, () -> 123L), 100),
            StorageTypes.ITEM,
            tracker,
            () -> {
            }
        );

        storage.insert(new ItemResource(Items.DIRT, createDummyTag()), 10, Action.EXECUTE, new PlayerActor("A"));
        storage.insert(new ItemResource(Items.GLASS, null), 15, Action.EXECUTE, EmptyActor.INSTANCE);

        // Act
        final CompoundTag serialized = sut.toTag(storage);
        final Storage deserialized = sut.fromTag(serialized, listener);

        // Assert
        assertThat(listener.isChanged()).isFalse();
        assertThat(deserialized).isInstanceOf(LimitedStorage.class);
        assertThat(((LimitedStorage) deserialized).getCapacity()).isEqualTo(100);
        assertThat(deserialized.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(new ItemResource(Items.DIRT, createDummyTag()), 10),
            new ResourceAmount(new ItemResource(Items.GLASS, null), 15)
        );
        assertThat(((TrackedStorage) deserialized).findTrackedResourceByActorType(
            new ItemResource(Items.DIRT, createDummyTag()),
            PlayerActor.class
        )).get().usingRecursiveComparison().isEqualTo(new TrackedResource("A", 123));
        assertThat(((TrackedStorage) deserialized).findTrackedResourceByActorType(
            new ItemResource(Items.GLASS, null),
            PlayerActor.class
        )).isEmpty();
    }
}
