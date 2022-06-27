package com.refinedmods.refinedstorage2.platform.test;

import net.minecraft.nbt.CompoundTag;

public final class TagHelper {
    private TagHelper() {
    }

    public static CompoundTag createDummyTag() {
        final CompoundTag tag = new CompoundTag();
        tag.putString("dummy", "tag");
        return tag;
    }
}
