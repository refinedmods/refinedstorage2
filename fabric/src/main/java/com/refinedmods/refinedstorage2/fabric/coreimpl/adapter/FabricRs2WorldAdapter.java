package com.refinedmods.refinedstorage2.fabric.coreimpl.adapter;

import com.refinedmods.refinedstorage2.core.Rs2World;
import com.refinedmods.refinedstorage2.core.util.Position;
import com.refinedmods.refinedstorage2.fabric.util.Positions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

public class FabricRs2WorldAdapter implements Rs2World {
    private final MinecraftServer server;
    private final RegistryKey<World> dimension;

    private FabricRs2WorldAdapter(MinecraftServer server, RegistryKey<World> dimension) {
        this.server = server;
        this.dimension = dimension;
    }

    public static FabricRs2WorldAdapter of(World world) {
        return new FabricRs2WorldAdapter(world.getServer(), world.getRegistryKey());
    }

    @Override
    public boolean isPowered(Position pos) {
        World world = server.getWorld(dimension);
        if (world != null) {
            return world.isReceivingRedstonePower(Positions.toBlockPos(pos));
        }
        return false;
    }
}
