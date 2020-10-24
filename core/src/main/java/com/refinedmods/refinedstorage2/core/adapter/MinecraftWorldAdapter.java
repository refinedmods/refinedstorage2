package com.refinedmods.refinedstorage2.core.adapter;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Optional;

public class MinecraftWorldAdapter implements WorldAdapter {
    private final World world;
    private final WorldIdentifier identifier;

    public MinecraftWorldAdapter(World world) {
        this.world = world;
        this.identifier = new MinecraftWorldIdentifier(world);
    }

    @Override
    public Optional<BlockEntity> getBlockEntity(BlockPos pos) {
        return Optional.ofNullable(world.getBlockEntity(pos));
    }

    @Override
    public WorldIdentifier getIdentifier() {
        return identifier;
    }
}
