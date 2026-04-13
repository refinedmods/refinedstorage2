package com.refinedmods.refinedstorage.fabric.security;

import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.support.network.NetworkNodeContainerProvider;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public class NetworkNodeBreakSecurityEventListener implements PlayerBlockBreakEvents.Before {
    @Override
    public boolean beforeBlockBreak(final Level world,
                                    final Player player,
                                    final BlockPos pos,
                                    final BlockState state,
                                    @Nullable final BlockEntity blockEntity) {
        final NetworkNodeContainerProvider provider = Platform.INSTANCE.getContainerProvider(world, pos, null);
        if (provider != null && player instanceof ServerPlayer serverPlayer && !provider.canBuild(serverPlayer)) {
            RefinedStorageApi.INSTANCE.sendNoPermissionMessage(
                serverPlayer,
                createTranslation("misc", "no_permission.build.break", state.getBlock().getName())
            );
            return false;
        }
        return true;
    }
}
