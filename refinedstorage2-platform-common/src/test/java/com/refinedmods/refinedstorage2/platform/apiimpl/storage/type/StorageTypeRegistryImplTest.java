package com.refinedmods.refinedstorage2.platform.apiimpl.storage.type;

import com.refinedmods.refinedstorage2.platform.api.storage.type.StorageTypeRegistry;
import com.refinedmods.refinedstorage2.test.Rs2Test;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Rs2Test
class StorageTypeRegistryImplTest {
    private static final ResourceLocation ITEM_ID = new ResourceLocation("item");

    StorageTypeRegistry sut = new StorageTypeRegistryImpl();

    @BeforeEach
    void setUp() {
        sut.addType(ITEM_ID, ItemStorageType.INSTANCE);
    }

    @Test
    void Test_adding_duplicates() {
        // Assert
        assertThrows(IllegalArgumentException.class, () -> sut.addType(ITEM_ID, FluidStorageType.INSTANCE));
    }

    @Test
    void Test_getting_identifier() {
        // Assert
        assertThat(sut.getIdentifier(ItemStorageType.INSTANCE))
                .get()
                .isEqualTo(ITEM_ID);

        assertThat(sut.getIdentifier(FluidStorageType.INSTANCE))
                .isEmpty();
    }

    @Test
    void Test_getting_type() {
        // Assert
        assertThat(sut.getType(ITEM_ID))
                .get()
                .isEqualTo(ItemStorageType.INSTANCE);

        assertThat(sut.getType(new ResourceLocation("any")))
                .isEmpty();
    }
}
