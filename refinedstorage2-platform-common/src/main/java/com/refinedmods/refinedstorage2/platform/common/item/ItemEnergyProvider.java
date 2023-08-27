package com.refinedmods.refinedstorage2.platform.common.item;

import com.refinedmods.refinedstorage2.platform.common.containermenu.slot.PlayerSlotReference;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface ItemEnergyProvider {
    boolean isEnabled();

    long getStored(ItemStack stack);

    long getCapacity(ItemStack stack);

    void drain(Player player, PlayerSlotReference slotReference, long amount);

    ItemStack getAtCapacity(ItemStack stack);
}
