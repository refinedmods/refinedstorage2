package com.refinedmods.refinedstorage2.core.item;

import java.util.Objects;

public final class Rs2TagInsensitiveItemStackIdentifier {
    private final Rs2Item item;

    public Rs2TagInsensitiveItemStackIdentifier(Rs2ItemStack stack) {
        this.item = stack.getItem();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rs2TagInsensitiveItemStackIdentifier that = (Rs2TagInsensitiveItemStackIdentifier) o;
        return Objects.equals(item, that.item);
    }

    @Override
    public int hashCode() {
        return Objects.hash(item);
    }
}
