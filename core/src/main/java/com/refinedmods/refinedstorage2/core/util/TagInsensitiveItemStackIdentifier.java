package com.refinedmods.refinedstorage2.core.util;

import java.util.Objects;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class TagInsensitiveItemStackIdentifier {
    private final Item item;

    public TagInsensitiveItemStackIdentifier(ItemStack stack) {
        this.item = stack.getItem();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TagInsensitiveItemStackIdentifier that = (TagInsensitiveItemStackIdentifier) o;
        return Objects.equals(item, that.item);
    }

    @Override
    public int hashCode() {
        return Objects.hash(item);
    }
}
