package com.refinedmods.refinedstorage.common.support.containermenu;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class DisabledSlot extends Slot {
    public DisabledSlot(final Container container, final int index, final int x, final int y) {
        super(container, index, x, y);
    }

    @Override
    public boolean mayPickup(final Player player) {
        return false;
    }

    @Override
    public boolean mayPlace(final ItemStack stack) {
        return false;
    }
}
