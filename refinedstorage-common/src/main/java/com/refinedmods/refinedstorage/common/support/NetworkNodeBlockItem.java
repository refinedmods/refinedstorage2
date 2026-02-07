package com.refinedmods.refinedstorage.common.support;

import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class NetworkNodeBlockItem extends BaseBlockItem {
    public NetworkNodeBlockItem(final Identifier id, final Block block, final Component helpText) {
        super(id, block, helpText);
    }

    public NetworkNodeBlockItem(final Block block, final Properties properties, @Nullable final Component helpText) {
        super(block, properties, helpText);
    }

    @Override
    protected boolean placeBlock(final BlockPlaceContext ctx, final BlockState state) {
        if (ctx.getPlayer() instanceof ServerPlayer serverPlayer && !(RefinedStorageApi.INSTANCE.canPlaceNetworkNode(
            serverPlayer,
            ctx.getLevel(),
            ctx.getClickedPos(),
            state
        ))) {
            return false;
        }
        return super.placeBlock(ctx, state);
    }
}
