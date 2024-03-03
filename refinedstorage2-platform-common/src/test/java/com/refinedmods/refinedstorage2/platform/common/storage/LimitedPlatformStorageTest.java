package com.refinedmods.refinedstorage2.platform.common.storage;

import com.refinedmods.refinedstorage2.api.storage.InMemoryStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorage;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorageImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LimitedPlatformStorageTest {
    LimitedPlatformStorage sut;

    @BeforeEach
    @SuppressWarnings("ConstantConditions")
    void setUp() {
        final LimitedStorageImpl delegate = new LimitedStorageImpl(new InMemoryStorageImpl(), 100);
        sut = new LimitedPlatformStorage(delegate, StorageTypes.ITEM, null, null);
    }

    @Test
    void testSetup() {
        // Assert
        assertThat(sut.getType()).isEqualTo(StorageTypes.ITEM);
        assertThat(sut).isInstanceOf(LimitedStorage.class);
        assertThat(sut.getCapacity()).isEqualTo(100);
    }
}
