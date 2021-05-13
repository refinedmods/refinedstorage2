package com.refinedmods.refinedstorage2.fabric.screenhandler.diskdrive;

import com.refinedmods.refinedstorage2.core.network.node.RedstoneMode;
import com.refinedmods.refinedstorage2.core.network.node.diskdrive.DiskDriveNetworkNode;
import com.refinedmods.refinedstorage2.core.storage.AccessMode;
import com.refinedmods.refinedstorage2.core.storage.disk.StorageDiskInfo;
import com.refinedmods.refinedstorage2.core.util.FilterMode;
import com.refinedmods.refinedstorage2.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.fabric.block.entity.AccessModeSettings;
import com.refinedmods.refinedstorage2.fabric.block.entity.FilterModeSettings;
import com.refinedmods.refinedstorage2.fabric.block.entity.RedstoneModeSettings;
import com.refinedmods.refinedstorage2.fabric.block.entity.diskdrive.DiskDriveBlockEntity;
import com.refinedmods.refinedstorage2.fabric.block.entity.diskdrive.DiskDriveInventory;
import com.refinedmods.refinedstorage2.fabric.screenhandler.AccessModeAccessor;
import com.refinedmods.refinedstorage2.fabric.screenhandler.BaseScreenHandler;
import com.refinedmods.refinedstorage2.fabric.screenhandler.ExactModeAccessor;
import com.refinedmods.refinedstorage2.fabric.screenhandler.FilterModeAccessor;
import com.refinedmods.refinedstorage2.fabric.screenhandler.PriorityAccessor;
import com.refinedmods.refinedstorage2.fabric.screenhandler.RedstoneModeAccessor;
import com.refinedmods.refinedstorage2.fabric.screenhandler.property.TwoWaySyncProperty;
import com.refinedmods.refinedstorage2.fabric.screenhandler.slot.FilterSlot;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import alexiil.mc.lib.attributes.item.FixedItemInv;
import alexiil.mc.lib.attributes.item.compat.SlotFixedItemInv;
import alexiil.mc.lib.attributes.item.impl.FullFixedItemInv;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class DiskDriveScreenHandler extends BaseScreenHandler implements PriorityAccessor, FilterModeAccessor, ExactModeAccessor, AccessModeAccessor, RedstoneModeAccessor {
    private static final int DISK_SLOT_X = 61;
    private static final int DISK_SLOT_Y = 54;

    private static final int FILTER_SLOT_X = 8;
    private static final int FILTER_SLOT_Y = 20;

    private final StorageDiskInfoAccessor storageDiskInfoAccessor;
    private final List<Slot> diskSlots = new ArrayList<>();
    private final TwoWaySyncProperty<Integer> priorityProperty;
    private final TwoWaySyncProperty<FilterMode> filterModeProperty;
    private final TwoWaySyncProperty<Boolean> exactModeProperty;
    private final TwoWaySyncProperty<AccessMode> accessModeProperty;
    private final TwoWaySyncProperty<RedstoneMode> redstoneModeProperty;

    public DiskDriveScreenHandler(int syncId, PlayerInventory playerInventory) {
        super(Rs2Mod.SCREEN_HANDLERS.getDiskDrive(), syncId);

        this.priorityProperty = TwoWaySyncProperty.forClient(
                0,
                priority -> priority,
                priority -> priority,
                0,
                priority -> {
                }
        );
        this.filterModeProperty = TwoWaySyncProperty.forClient(
                1,
                FilterModeSettings::getFilterMode,
                FilterModeSettings::getFilterMode,
                FilterMode.BLOCK,
                filterMode -> {
                }
        );
        this.exactModeProperty = TwoWaySyncProperty.forClient(
                2,
                value -> Boolean.TRUE.equals(value) ? 0 : 1,
                value -> value == 0,
                true,
                exactMode -> {
                }
        );
        this.accessModeProperty = TwoWaySyncProperty.forClient(
                3,
                AccessModeSettings::getAccessMode,
                AccessModeSettings::getAccessMode,
                AccessMode.INSERT_EXTRACT,
                accessMode -> {
                }
        );
        this.redstoneModeProperty = TwoWaySyncProperty.forClient(
                4,
                RedstoneModeSettings::getRedstoneMode,
                RedstoneModeSettings::getRedstoneMode,
                RedstoneMode.IGNORE,
                redstoneMode -> {
                }
        );

        addProperty(priorityProperty);
        addProperty(filterModeProperty);
        addProperty(exactModeProperty);
        addProperty(accessModeProperty);
        addProperty(redstoneModeProperty);

        this.storageDiskInfoAccessor = new StorageDiskInfoAccessorImpl(playerInventory.player.getEntityWorld());

        addSlots(playerInventory.player, new DiskDriveInventory(), new FullFixedItemInv(9));
    }

    public DiskDriveScreenHandler(int syncId, PlayerEntity player, FixedItemInv diskInventory, FixedItemInv filterInventory, DiskDriveBlockEntity diskDrive, StorageDiskInfoAccessor storageDiskInfoAccessor) {
        super(Rs2Mod.SCREEN_HANDLERS.getDiskDrive(), syncId);

        this.priorityProperty = TwoWaySyncProperty.forServer(
                0,
                priority -> priority,
                priority -> priority,
                diskDrive::getPriority,
                diskDrive::setPriority
        );
        this.filterModeProperty = TwoWaySyncProperty.forServer(
                1,
                FilterModeSettings::getFilterMode,
                FilterModeSettings::getFilterMode,
                diskDrive::getFilterMode,
                diskDrive::setFilterMode
        );
        this.exactModeProperty = TwoWaySyncProperty.forServer(
                2,
                value -> Boolean.TRUE.equals(value) ? 0 : 1,
                value -> value == 0,
                diskDrive::isExactMode,
                diskDrive::setExactMode
        );
        this.accessModeProperty = TwoWaySyncProperty.forServer(
                3,
                AccessModeSettings::getAccessMode,
                AccessModeSettings::getAccessMode,
                diskDrive::getAccessMode,
                diskDrive::setAccessMode
        );
        this.redstoneModeProperty = TwoWaySyncProperty.forServer(
                4,
                RedstoneModeSettings::getRedstoneMode,
                RedstoneModeSettings::getRedstoneMode,
                diskDrive::getRedstoneMode,
                diskDrive::setRedstoneMode
        );

        addProperty(priorityProperty);
        addProperty(filterModeProperty);
        addProperty(exactModeProperty);
        addProperty(accessModeProperty);
        addProperty(redstoneModeProperty);

        this.storageDiskInfoAccessor = storageDiskInfoAccessor;

        addSlots(player, diskInventory, filterInventory);
    }

    private void addSlots(PlayerEntity player, FixedItemInv diskInventory, FixedItemInv filterInventory) {
        for (int i = 0; i < DiskDriveNetworkNode.DISK_COUNT; ++i) {
            diskSlots.add(addSlot(createDiskSlot(player, diskInventory, i)));
        }
        for (int i = 0; i < 9; ++i) {
            addSlot(createFilterSlot(player, filterInventory, i));
        }
        addPlayerInventory(player.inventory, 8, 141);
    }

    private SlotFixedItemInv createFilterSlot(PlayerEntity player, FixedItemInv filterInventory, int i) {
        int x = FILTER_SLOT_X + (18 * i);
        return new FilterSlot(this, filterInventory, !player.world.isClient(), i, x, FILTER_SLOT_Y);
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

    public long getCapacity() {
        return getStorageDiskInfo().mapToLong(StorageDiskInfo::getCapacity).sum();
    }

    public long getStored() {
        return getStorageDiskInfo().mapToLong(StorageDiskInfo::getStored).sum();
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

    @Override
    public int getPriority() {
        return priorityProperty.getDeserialized();
    }

    @Override
    public void setPriority(int priority) {
        priorityProperty.syncToServer(priority);
    }

    @Override
    public FilterMode getFilterMode() {
        return filterModeProperty.getDeserialized();
    }

    @Override
    public void setFilterMode(FilterMode filterMode) {
        filterModeProperty.syncToServer(filterMode);
    }

    @Override
    public boolean isExactMode() {
        return exactModeProperty.getDeserialized();
    }

    @Override
    public void setExactMode(boolean exactMode) {
        exactModeProperty.syncToServer(exactMode);
    }

    @Override
    public AccessMode getAccessMode() {
        return accessModeProperty.getDeserialized();
    }

    @Override
    public void setAccessMode(AccessMode accessMode) {
        accessModeProperty.syncToServer(accessMode);
    }

    @Override
    public RedstoneMode getRedstoneMode() {
        return redstoneModeProperty.getDeserialized();
    }

    @Override
    public void setRedstoneMode(RedstoneMode redstoneMode) {
        redstoneModeProperty.syncToServer(redstoneMode);
    }
}
