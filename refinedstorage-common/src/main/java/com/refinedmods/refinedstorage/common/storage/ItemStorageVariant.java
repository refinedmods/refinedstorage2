package com.refinedmods.refinedstorage.common.storage;

import com.refinedmods.refinedstorage.common.content.Items;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import org.jspecify.annotations.Nullable;

public enum ItemStorageVariant implements StringRepresentable, StorageVariant {
    ONE_K("1k", 1024L),
    FOUR_K("4k", 1024 * 4L),
    SIXTEEN_K("16k", 1024 * 4 * 4L),
    SIXTY_FOUR_K("64k", 1024 * 4 * 4 * 4L),
    CREATIVE("creative", null);

    private final String name;
    @Nullable
    private final Long capacity;

    ItemStorageVariant(final String name, @Nullable final Long capacity) {
        this.name = name;
        this.capacity = capacity;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    @Nullable
    public Long getCapacity() {
        return capacity;
    }

    @Nullable
    @Override
    public Item getStoragePart() {
        if (this == CREATIVE) {
            return null;
        }
        return Items.INSTANCE.getItemStoragePart(this);
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
