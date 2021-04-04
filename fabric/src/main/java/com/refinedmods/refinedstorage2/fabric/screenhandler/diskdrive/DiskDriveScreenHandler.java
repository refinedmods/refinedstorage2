package com.refinedmods.refinedstorage2.fabric.screenhandler.diskdrive;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import alexiil.mc.lib.attributes.item.FixedItemInv;
import alexiil.mc.lib.attributes.item.compat.SlotFixedItemInv;
import com.refinedmods.refinedstorage2.core.storage.disk.StorageDiskInfo;
import com.refinedmods.refinedstorage2.fabric.RefinedStorage2Mod;
import com.refinedmods.refinedstorage2.fabric.block.entity.diskdrive.DiskDriveInventory;
import com.refinedmods.refinedstorage2.fabric.screenhandler.BaseScreenHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class DiskDriveScreenHandler extends BaseScreenHandler {
    private static final int DISK_SLOT_X = 61;
    private static final int DISK_SLOT_Y = 54;

    private final StorageDiskInfoAccessor storageDiskInfoAccessor;
    private final List<Slot> diskSlots = new ArrayList<>();

    public DiskDriveScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory.player, new DiskDriveInventory(), new StorageDiskInfoAccessorImpl(playerInventory.player.getEntityWorld()));
    }

    public DiskDriveScreenHandler(int syncId, PlayerEntity player, FixedItemInv diskInventory, StorageDiskInfoAccessor storageDiskInfoAccessor) {
        super(RefinedStorage2Mod.SCREEN_HANDLERS.getDiskDrive(), syncId);

        this.storageDiskInfoAccessor = storageDiskInfoAccessor;

        for (int i = 0; i < 8; ++i) {
            diskSlots.add(addSlot(createDiskSlot(player, diskInventory, i)));
        }

        addPlayerInventory(player.inventory, 8, 141);
    }

    private SlotFixedItemInv createDiskSlot(PlayerEntity player, FixedItemInv diskInventory, int i) {
        int x = DISK_SLOT_X + ((i % 2) * 18);
        int y = DISK_SLOT_Y + Math.floorDiv(i, 2) * 18;
        return new SlotFixedItemInv(this, diskInventory, !player.world.isClient(), i, x, y);
    }

    public boolean hasInfiniteDisk() {
        return getStorageDiskInfo().anyMatch(info -> info.getCapacity() == -1);
    }

    public double getProgress() {
        if (hasInfiniteDisk()) {
            return 0;
        }
        return (double) getStored() / (double) getCapacity();
    }

    public int getCapacity() {
        return getStorageDiskInfo().mapToInt(StorageDiskInfo::getCapacity).sum();
    }

    public int getStored() {
        return getStorageDiskInfo().mapToInt(StorageDiskInfo::getStored).sum();
    }

    private Stream<StorageDiskInfo> getStorageDiskInfo() {
        return diskSlots
            .stream()
            .map(Slot::getStack)
            .filter(stack -> !stack.isEmpty())
            .map(storageDiskInfoAccessor::getDiskInfo)
            .flatMap(info -> info.map(Stream::of).orElseGet(Stream::empty));
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
