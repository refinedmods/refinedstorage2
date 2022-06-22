package com.refinedmods.refinedstorage2.platform.apiimpl.storage;

import com.refinedmods.refinedstorage2.api.storage.InMemoryStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorage;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorageImpl;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.apiimpl.storage.type.ItemStorageType;
import com.refinedmods.refinedstorage2.test.Rs2Test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Rs2Test
class LimitedPlatformStorageTest {
    LimitedPlatformStorage<ItemResource> sut;

    @BeforeEach
    void setUp() {
        LimitedStorageImpl<ItemResource> delegate = new LimitedStorageImpl<>(new InMemoryStorageImpl<>(), 100);
        sut = new LimitedPlatformStorage<>(delegate, ItemStorageType.INSTANCE, null, null);
    }

    @Test
    void Test_setup() {
        // Assert
        assertThat(sut.getType()).isEqualTo(ItemStorageType.INSTANCE);
        assertThat(sut).isInstanceOf(LimitedStorage.class);
        assertThat(sut.getCapacity()).isEqualTo(100);
    }
}
