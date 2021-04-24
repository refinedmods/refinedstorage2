package com.refinedmods.refinedstorage2.fabric.coreimpl.adapter;

import com.refinedmods.refinedstorage2.core.World;
import com.refinedmods.refinedstorage2.core.util.Position;
import com.refinedmods.refinedstorage2.fabric.util.Positions;
import net.minecraft.server.MinecraftServer;
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
    public boolean isPowered(Position pos) {
        net.minecraft.world.World world = server.getWorld(dimension);
        if (world != null) {
            return world.isReceivingRedstonePower(Positions.toBlockPos(pos));
        }
        return false;
    }
}
