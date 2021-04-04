package com.refinedmods.refinedstorage2.fabric.screenhandler;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
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
    public boolean canUse(PlayerEntity player) {
        return true;
    }
}
