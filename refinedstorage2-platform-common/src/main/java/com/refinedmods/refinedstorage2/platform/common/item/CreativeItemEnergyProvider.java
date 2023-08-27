package com.refinedmods.refinedstorage2.platform.common.item;

import com.refinedmods.refinedstorage2.platform.common.containermenu.slot.PlayerSlotReference;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class CreativeItemEnergyProvider implements ItemEnergyProvider {
    public static final ItemEnergyProvider INSTANCE = new CreativeItemEnergyProvider();

    private CreativeItemEnergyProvider() {
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public long getStored(final ItemStack stack) {
        return 0;
    }

    @Override
    public long getCapacity(final ItemStack stack) {
        return 0;
    }

    @Override
    public void drain(final Player player, final PlayerSlotReference slotReference, final long amount) {
        // no op
    }

    @Override
    public ItemStack getAtCapacity(final ItemStack stack) {
        return stack.copy();
    }
}
