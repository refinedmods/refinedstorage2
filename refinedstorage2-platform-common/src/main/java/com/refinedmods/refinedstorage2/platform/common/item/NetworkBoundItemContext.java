package com.refinedmods.refinedstorage2.platform.common.item;

import com.refinedmods.refinedstorage2.platform.common.containermenu.slot.PlayerSlotReference;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class NetworkBoundItemContext {
    private final Player player;
    private final PlayerSlotReference playerSlotReference;
    private final ItemEnergyProvider energyProvider;
    @Nullable
    private final NetworkReference networkReference;

    public NetworkBoundItemContext(
        final Player player,
        final PlayerSlotReference playerSlotReference,
        final ItemEnergyProvider energyProvider,
        @Nullable final NetworkReference networkReference
    ) {
        this.player = player;
        this.playerSlotReference = playerSlotReference;
        this.energyProvider = energyProvider;
        this.networkReference = networkReference;
    }

    public boolean isActive() {
        if (!energyProvider.isEnabled()) {
            return true;
        }
        return energyProvider.getStored(player.getInventory().getItem(playerSlotReference.getSlotIndex())) > 0;
    }

    public void drain(final long amount) {
        energyProvider.drain(player, playerSlotReference, amount);
    }

    @Nullable
    public NetworkReference getNetworkReference() {
        return networkReference;
    }

    public record NetworkReference(ResourceKey<Level> dimensionKey, BlockPos pos) {
    }
}
