package com.refinedmods.refinedstorage2.platform.common.internal.storage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.InMemoryStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorage;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.tracked.InMemoryTrackedStorageRepository;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorageRepository;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.storage.PlayerActor;
import com.refinedmods.refinedstorage2.platform.common.SimpleListener;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.type.ItemStorageType;
import com.refinedmods.refinedstorage2.platform.test.SetupMinecraft;

import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

@SetupMinecraft
class PlatformStorageTest {
    PlatformStorage<ItemResource> sut;
    SimpleListener listener;

    @BeforeEach
    void setUp() {
        final TrackedStorageRepository<ItemResource> trackedStorageRepository =
            new InMemoryTrackedStorageRepository<>();
        final TrackedStorageImpl<ItemResource> delegate = new TrackedStorageImpl<>(
            new LimitedStorageImpl<>(new InMemoryStorageImpl<>(), 100),
            trackedStorageRepository,
            () -> 0L
        );
        listener = new SimpleListener();
        sut = new PlatformStorage<>(delegate, ItemStorageType.INSTANCE, trackedStorageRepository, listener);
    }

    @Test
    void testInitialState() {
        // Assert
        assertThat(sut.getType()).isEqualTo(ItemStorageType.INSTANCE);
        assertThat(sut).isNotInstanceOf(LimitedStorage.class);
    }

    @Test
    void shouldLoadAndUpdateTrackedResources() {
        // Act
        sut.load(new ItemResource(Items.DIRT, null), 10, "A", 100);
        sut.load(new ItemResource(Items.GLASS, null), 20, null, 200);
        sut.load(new ItemResource(Items.STONE, null), 30, "", 300);

        // Assert
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>(new ItemResource(Items.DIRT, null), 10),
            new ResourceAmount<>(new ItemResource(Items.GLASS, null), 20),
            new ResourceAmount<>(new ItemResource(Items.STONE, null), 30)
        );
        assertThat(sut.findTrackedResourceByActorType(new ItemResource(Items.DIRT, null), PlayerActor.class))
            .get()
            .usingRecursiveComparison()
            .isEqualTo(new TrackedResource("A", 100));
        assertThat(
            sut.findTrackedResourceByActorType(new ItemResource(Items.GLASS, null), PlayerActor.class)).isEmpty();
        assertThat(
            sut.findTrackedResourceByActorType(new ItemResource(Items.STONE, null), PlayerActor.class)).isEmpty();
        assertThat(listener.getChanges()).isZero();
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldInsert(final Action action) {
        // Act
        sut.insert(new ItemResource(Items.DIRT, null), 10, action, new PlayerActor("A"));
        sut.insert(new ItemResource(Items.DIRT, null), 95, action, new PlayerActor("A"));
        sut.insert(new ItemResource(Items.DIRT, null), 1, action, new PlayerActor("A"));

        // Assert
        if (action == Action.EXECUTE) {
            assertThat(listener.getChanges()).isEqualTo(2);
            assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount<>(new ItemResource(Items.DIRT, null), 100)
            );
            assertThat(sut.findTrackedResourceByActorType(new ItemResource(Items.DIRT, null), PlayerActor.class))
                .get()
                .usingRecursiveComparison()
                .isEqualTo(new TrackedResource("A", 0));
        } else {
            assertThat(listener.getChanges()).isZero();
            assertThat(sut.getAll()).isEmpty();
            assertThat(
                sut.findTrackedResourceByActorType(new ItemResource(Items.DIRT, null), PlayerActor.class)).isEmpty();
        }
    }
}
