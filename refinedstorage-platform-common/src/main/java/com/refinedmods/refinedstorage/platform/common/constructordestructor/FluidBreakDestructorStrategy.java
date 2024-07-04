package com.refinedmods.refinedstorage.platform.common.constructordestructor;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.resource.filter.Filter;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage.platform.api.constructordestructor.DestructorStrategy;
import com.refinedmods.refinedstorage.platform.common.Platform;
import com.refinedmods.refinedstorage.platform.common.support.resource.FluidResource;

import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;

class FluidBreakDestructorStrategy implements DestructorStrategy {
    private final ServerLevel level;
    private final BlockPos pos;

    FluidBreakDestructorStrategy(final ServerLevel level, final BlockPos pos) {
        this.level = level;
        this.pos = pos;
    }

    @Override
    public boolean apply(final Filter filter,
                         final Actor actor,
                         final Supplier<Network> networkSupplier,
                         final Player actingPlayer) {
        if (!level.isLoaded(pos)) {
            return false;
        }
        final BlockState blockState = level.getBlockState(pos);
        if (!(blockState.getBlock() instanceof LiquidBlock liquidBlock)
            || blockState.getValue(LiquidBlock.LEVEL) != 0) {
            return false;
        }
        final Fluid fluid = blockState.getFluidState().getType();
        final FluidResource fluidResource = new FluidResource(fluid);
        if (!filter.isAllowed(fluidResource)) {
            return false;
        }
        return tryInsert(actor, networkSupplier, actingPlayer, blockState, liquidBlock, fluidResource);
    }

    private boolean tryInsert(final Actor actor,
                              final Supplier<Network> networkSupplier,
                              final Player actingPlayer,
                              final BlockState blockState,
                              final LiquidBlock liquidBlock,
                              final FluidResource fluidResource) {
        final long amount = Platform.INSTANCE.getBucketAmount();
        final long inserted = getStorageChannel(networkSupplier).insert(fluidResource, amount, Action.SIMULATE, actor);
        if (inserted != amount) {
            return false;
        }
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 11);
        Platform.INSTANCE.getBucketPickupSound(liquidBlock, blockState).ifPresent(
            sound -> level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F)
        );
        level.gameEvent(actingPlayer, GameEvent.FLUID_PICKUP, pos);
        getStorageChannel(networkSupplier).insert(fluidResource, amount, Action.EXECUTE, actor);
        return true;
    }

    private StorageChannel getStorageChannel(final Supplier<Network> networkSupplier) {
        return networkSupplier.get().getComponent(StorageNetworkComponent.class);
    }
}
