package com.refinedmods.refinedstorage2.platform.fabric.screenhandler.slot;

import alexiil.mc.lib.attributes.item.FixedItemInv;
import alexiil.mc.lib.attributes.item.compat.SlotFixedItemInv;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;

public class FilterSlot extends SlotFixedItemInv {
    public FilterSlot(ScreenHandler container, FixedItemInv inv, boolean server, int slotIndex, int x, int y) {
        super(container, inv, server, slotIndex, x, y);
    }

    @Override
    public void setStack(ItemStack stack) {
        if (!stack.isEmpty()) {
            stack.setCount(1);
        }
        super.setStack(stack);
    }

    @Override
    public boolean canTakeItems(PlayerEntity playerEntity) {
        return false;
    }
}
