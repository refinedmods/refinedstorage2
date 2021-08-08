package com.refinedmods.refinedstorage2.fabric.screenhandler;

import com.refinedmods.refinedstorage2.fabric.screenhandler.slot.FilterSlot;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.jetbrains.annotations.Nullable;

public class BaseScreenHandler extends ScreenHandler {
    protected BaseScreenHandler(@Nullable ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    protected void addPlayerInventory(PlayerInventory inventory, int xInventory, int yInventory) {
        int id = 9;

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                addSlot(new Slot(inventory, id++, xInventory + x * 18, yInventory + y * 18));
            }
        }

        id = 0;

        for (int i = 0; i < 9; i++) {
            int x = xInventory + i * 18;
            int y = yInventory + 4 + (3 * 18);

            addSlot(new Slot(inventory, id++, x, y));
        }
    }

    @Override
    public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
        return !(slot instanceof FilterSlot);
    }

    @Override
    public void onSlotClick(int id, int dragType, SlotActionType actionType, PlayerEntity player) {
        Slot slot = id >= 0 ? getSlot(id) : null;

        if (slot instanceof FilterSlot) {
            ItemStack cursorStack = getCursorStack();
            if (cursorStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.setStack(cursorStack.copy());
            }
        } else {
            super.onSlotClick(id, dragType, actionType, player);
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }
}
