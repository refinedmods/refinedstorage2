package com.refinedmods.refinedstorage2.fabric.coreimpl.adapter;

import java.util.Optional;

import com.refinedmods.refinedstorage2.core.World;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;

public class FabricWorldAdapter implements World {
    private final MinecraftServer server;
    private final RegistryKey<net.minecraft.world.World> dimension;

    private FabricWorldAdapter(MinecraftServer server, RegistryKey<net.minecraft.world.World> dimension) {
        this.server = server;
        this.dimension = dimension;
    }

    public static FabricWorldAdapter of(net.minecraft.world.World world) {
        return new FabricWorldAdapter(world.getServer(), world.getRegistryKey());
    }

    @Override
    public Optional<BlockEntity> getBlockEntity(BlockPos pos) {
        net.minecraft.world.World world = server.getWorld(dimension);
        if (world != null) {
            return Optional.ofNullable(world.getBlockEntity(pos));
        }
        return Optional.empty();
    }

    @Override
    public boolean isPowered(BlockPos pos) {
        net.minecraft.world.World world = server.getWorld(dimension);
        if (world != null) {
            return world.isReceivingRedstonePower(pos);
        }
        return false;
    }
}
