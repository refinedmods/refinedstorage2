package com.refinedmods.refinedstorage2.fabric.coreimpl.network.node;

import com.refinedmods.refinedstorage2.core.network.node.NetworkNode;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeReference;

import java.util.Optional;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.dynamic.GlobalPos;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FabricNetworkNodeReference implements NetworkNodeReference {
    private final MinecraftServer server;
    private final GlobalPos globalPos;

    private FabricNetworkNodeReference(MinecraftServer server, GlobalPos globalPos) {
        this.server = server;
        this.globalPos = globalPos;
    }

    public static FabricNetworkNodeReference of(World world, BlockPos pos) {
        return new FabricNetworkNodeReference(world.getServer(), GlobalPos.create(world.getRegistryKey(), pos));
    }

    @Override
    public Optional<NetworkNode> get() {
        World world = server.getWorld(globalPos.getDimension());
        if (world != null) {
            BlockEntity blockEntity = world.getBlockEntity(globalPos.getPos());
            if (blockEntity instanceof NetworkNode) {
                return Optional.of((NetworkNode) blockEntity);
            }
        }

        return Optional.empty();
    }
}
