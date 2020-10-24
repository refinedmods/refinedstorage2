package com.refinedmods.refinedstorage2.core.adapter;

import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.Objects;

public class MinecraftWorldIdentifier implements WorldIdentifier {
    private final Identifier identifier;

    public MinecraftWorldIdentifier(World world) {
        this.identifier = world.getRegistryKey().getValue();
    }

    @Override
    public Identifier getId() {
        return identifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MinecraftWorldIdentifier that = (MinecraftWorldIdentifier) o;
        return identifier.equals(that.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier);
    }
}
