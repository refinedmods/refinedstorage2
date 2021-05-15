package com.refinedmods.refinedstorage2.fabric.coreimpl.network.node;

import com.refinedmods.refinedstorage2.core.network.node.NetworkNode;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeReference;

import java.util.Optional;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.GlobalPos;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

public class FabricNetworkNodeReference implements NetworkNodeReference {
    private static final String TAG_POSITION = "pos";
    private static final String TAG_DIMENSION = "dim";

    private final MinecraftServer server;
    private final GlobalPos globalPos;

    private FabricNetworkNodeReference(MinecraftServer server, GlobalPos globalPos) {
        this.server = server;
        this.globalPos = globalPos;
    }

    public static FabricNetworkNodeReference of(World world, BlockPos pos) {
        return new FabricNetworkNodeReference(world.getServer(), GlobalPos.create(world.getRegistryKey(), pos));
    }

    public static FabricNetworkNodeReference of(MinecraftServer server, CompoundTag tag) {
        BlockPos position = BlockPos.fromLong(tag.getLong(TAG_POSITION));
        Identifier dimension = new Identifier(tag.getString(TAG_DIMENSION));
        RegistryKey<World> dimensionKey = RegistryKey.of(Registry.DIMENSION, dimension);
        return new FabricNetworkNodeReference(server, GlobalPos.create(dimensionKey, position));
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putLong(TAG_POSITION, globalPos.getPos().asLong());
        tag.putString(TAG_DIMENSION, globalPos.getDimension().getValue().toString());
        return tag;
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
