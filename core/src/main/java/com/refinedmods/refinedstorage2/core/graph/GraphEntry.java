package com.refinedmods.refinedstorage2.core.graph;

import com.refinedmods.refinedstorage2.core.adapter.WorldIdentifier;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;

public class GraphEntry<T> {
    private final WorldIdentifier worldIdentifier;
    private final BlockPos pos;
    private final T element;

    public GraphEntry(WorldIdentifier worldIdentifier, BlockPos pos, T element) {
        this.worldIdentifier = worldIdentifier;
        this.pos = pos;
        this.element = element;
    }

    public WorldIdentifier getWorldIdentifier() {
        return worldIdentifier;
    }

    public BlockPos getPos() {
        return pos;
    }

    public T getElement() {
        return element;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GraphEntry<?> that = (GraphEntry<?>) o;
        return worldIdentifier.equals(that.worldIdentifier) && pos.equals(that.pos) && element.equals(that.element);
    }

    @Override
    public int hashCode() {
        return Objects.hash(worldIdentifier, pos, element);
    }
}
