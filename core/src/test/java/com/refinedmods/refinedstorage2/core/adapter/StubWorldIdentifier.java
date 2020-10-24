package com.refinedmods.refinedstorage2.core.adapter;

import net.minecraft.util.Identifier;

import java.util.Objects;

public class StubWorldIdentifier implements WorldIdentifier {
    private final Identifier identifier;

    public StubWorldIdentifier(Identifier identifier) {
        this.identifier = identifier;
    }

    @Override
    public Identifier getId() {
        return identifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StubWorldIdentifier that = (StubWorldIdentifier) o;
        return identifier.equals(that.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier);
    }
}
