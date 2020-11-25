package com.refinedmods.refinedstorage2.fabric.screen.handler;

import alexiil.mc.lib.attributes.item.FixedItemInv;
import alexiil.mc.lib.attributes.item.compat.SlotFixedItemInv;
import com.refinedmods.refinedstorage2.fabric.RefinedStorage2Mod;
import com.refinedmods.refinedstorage2.fabric.block.entity.diskdrive.DiskDriveInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class DiskDriveScreenHandler extends BaseScreenHandler {
    public DiskDriveScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory.player, new DiskDriveInventory());
    }

    public DiskDriveScreenHandler(int syncId, PlayerEntity player, FixedItemInv diskInventory) {
        super(RefinedStorage2Mod.SCREEN_HANDLERS.getDiskDrive(), syncId);

        int x = 80;
        int y = 54;

        for (int i = 0; i < 8; ++i) {
            addSlot(new SlotFixedItemInv(
                this,
                diskInventory,
                !player.world.isClient(),
                i,
                x + ((i % 2) * 18),
                y + Math.floorDiv(i, 2) * 18
            ));
        }

        addPlayerInventory(player, 8, 141);
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int index) {
        ItemStack originalStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasStack()) {
            ItemStack stackInSlot = slot.getStack();
            originalStack = stackInSlot.copy();

            if (index < 8) {
                if (!insertItem(stackInSlot, 8, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!insertItem(stackInSlot, 0, 8, false)) {
                return ItemStack.EMPTY;
            }

            if (stackInSlot.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }

        return originalStack;
    }
}
