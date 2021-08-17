package com.refinedmods.refinedstorage2.core.stack.item;

import java.util.Objects;

public final class Rs2ItemStackIdentifier {
    private final Rs2Item item;
    private final Object tag;

    public Rs2ItemStackIdentifier(Rs2ItemStack stack) {
        this.item = stack.getItem();
        this.tag = stack.getTag();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rs2ItemStackIdentifier that = (Rs2ItemStackIdentifier) o;
        return Objects.equals(item, that.item) &&
                Objects.equals(tag, that.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(item, tag);
    }
}
