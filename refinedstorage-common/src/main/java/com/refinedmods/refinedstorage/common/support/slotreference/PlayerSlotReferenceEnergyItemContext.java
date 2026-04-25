package com.refinedmods.refinedstorage.common.support.slotreference;

import com.refinedmods.refinedstorage.common.api.support.energy.EnergyItemContext;
import com.refinedmods.refinedstorage.common.api.support.slotreference.PlayerSlotReference;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class PlayerSlotReferenceEnergyItemContext implements EnergyItemContext {
    private final Player player;
    private final PlayerSlotReference playerSlotReference;

    public PlayerSlotReferenceEnergyItemContext(final Player player, final PlayerSlotReference playerSlotReference) {
        this.player = player;
        this.playerSlotReference = playerSlotReference;
    }

    @Override
    public ItemStack copyStack() {
        return playerSlotReference.get(player).copy();
    }

    @Override
    public void setStack(final ItemStack stack) {
        playerSlotReference.set(player, stack);
    }
}
