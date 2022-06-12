package com.refinedmods.refinedstorage2.platform.apiimpl.storage;

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
import com.refinedmods.refinedstorage2.platform.api.storage.PlayerSource;
import com.refinedmods.refinedstorage2.platform.apiimpl.storage.type.ItemStorageType;
import com.refinedmods.refinedstorage2.platform.test.SetupMinecraft;
import com.refinedmods.refinedstorage2.test.Rs2Test;

import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

@Rs2Test
@SetupMinecraft
class PlatformStorageTest {
    PlatformStorage<ItemResource> sut;
    Listener listener;

    @BeforeEach
    void setUp() {
        TrackedStorageRepository<ItemResource> trackedStorageRepository = new InMemoryTrackedStorageRepository<>();
        TrackedStorageImpl<ItemResource> delegate = new TrackedStorageImpl<>(new LimitedStorageImpl<>(new InMemoryStorageImpl<>(), 100), trackedStorageRepository, () -> 0L);
        listener = new Listener();
        sut = new PlatformStorage<>(delegate, ItemStorageType.INSTANCE, trackedStorageRepository, listener);
    }

    @Test
    void Test_setup() {
        // Assert
        assertThat(sut.getType()).isEqualTo(ItemStorageType.INSTANCE);
        assertThat(sut).isNotInstanceOf(LimitedStorage.class);
    }

    @Test
    void Test_loading() {
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
        assertThat(sut.findTrackedResourceBySourceType(new ItemResource(Items.DIRT, null), PlayerSource.class))
                .get()
                .usingRecursiveComparison()
                .isEqualTo(new TrackedResource("A", 100));
        assertThat(sut.findTrackedResourceBySourceType(new ItemResource(Items.GLASS, null), PlayerSource.class)).isEmpty();
        assertThat(sut.findTrackedResourceBySourceType(new ItemResource(Items.STONE, null), PlayerSource.class)).isEmpty();
        assertThat(listener.changes).isZero();
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_inserting(Action action) {
        // Act
        sut.insert(new ItemResource(Items.DIRT, null), 10, action, new PlayerSource("A"));
        sut.insert(new ItemResource(Items.DIRT, null), 95, action, new PlayerSource("A"));
        sut.insert(new ItemResource(Items.DIRT, null), 1, action, new PlayerSource("A"));

        // Assert
        if (action == Action.EXECUTE) {
            assertThat(listener.changes).isEqualTo(2);
            assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ResourceAmount<>(new ItemResource(Items.DIRT, null), 100)
            );
            assertThat(sut.findTrackedResourceBySourceType(new ItemResource(Items.DIRT, null), PlayerSource.class))
                    .get()
                    .usingRecursiveComparison()
                    .isEqualTo(new TrackedResource("A", 0));
        } else {
            assertThat(listener.changes).isZero();
            assertThat(sut.getAll()).isEmpty();
            assertThat(sut.findTrackedResourceBySourceType(new ItemResource(Items.DIRT, null), PlayerSource.class)).isEmpty();
        }
    }

    private static class Listener implements Runnable {
        private int changes;

        @Override
        public void run() {
            changes++;
        }
    }
}
