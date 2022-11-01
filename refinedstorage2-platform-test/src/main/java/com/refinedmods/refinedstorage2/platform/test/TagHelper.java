package com.refinedmods.refinedstorage2.platform.test;

import net.minecraft.nbt.CompoundTag;

public final class TagHelper {
    private TagHelper() {
    }

    public static CompoundTag createDummyTag() {
        return createDummyTag("tag");
    }

    public static CompoundTag createDummyTag(final String id) {
        final CompoundTag tag = new CompoundTag();
        tag.putString("dummy", id);
        return tag;
    }
}
