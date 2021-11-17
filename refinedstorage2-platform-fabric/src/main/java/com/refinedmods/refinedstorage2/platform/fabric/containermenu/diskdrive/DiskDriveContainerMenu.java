package com.refinedmods.refinedstorage2.platform.fabric.containermenu.diskdrive;

import com.refinedmods.refinedstorage2.api.core.filter.FilterMode;
import com.refinedmods.refinedstorage2.api.network.node.diskdrive.DiskDriveNetworkNode;
import com.refinedmods.refinedstorage2.api.storage.AccessMode;
import com.refinedmods.refinedstorage2.api.storage.StorageInfo;
import com.refinedmods.refinedstorage2.platform.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.platform.fabric.api.network.node.RedstoneMode;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.item.StorageDiskItem;
import com.refinedmods.refinedstorage2.platform.fabric.block.entity.AccessModeSettings;
import com.refinedmods.refinedstorage2.platform.fabric.block.entity.FilterModeSettings;
import com.refinedmods.refinedstorage2.platform.fabric.block.entity.RedstoneModeSettings;
import com.refinedmods.refinedstorage2.platform.fabric.block.entity.diskdrive.DiskDriveBlockEntity;
import com.refinedmods.refinedstorage2.platform.fabric.containermenu.AccessModeAccessor;
import com.refinedmods.refinedstorage2.platform.fabric.containermenu.BaseContainerMenu;
import com.refinedmods.refinedstorage2.platform.fabric.containermenu.ExactModeAccessor;
import com.refinedmods.refinedstorage2.platform.fabric.containermenu.FilterModeAccessor;
import com.refinedmods.refinedstorage2.platform.fabric.containermenu.PriorityAccessor;
import com.refinedmods.refinedstorage2.platform.fabric.containermenu.RedstoneModeAccessor;
import com.refinedmods.refinedstorage2.platform.fabric.containermenu.property.TwoWaySyncProperty;
import com.refinedmods.refinedstorage2.platform.fabric.containermenu.slot.FilterSlot;
import com.refinedmods.refinedstorage2.platform.fabric.containermenu.slot.ValidatedSlot;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class DiskDriveContainerMenu extends BaseContainerMenu implements PriorityAccessor, FilterModeAccessor, ExactModeAccessor, AccessModeAccessor, RedstoneModeAccessor {
    private static final int DISK_SLOT_X = 61;
    private static final int DISK_SLOT_Y = 54;

    private static final int FILTER_SLOT_X = 8;
    private static final int FILTER_SLOT_Y = 20;

    private final StorageInfoAccessor storageInfoAccessor;
    private final List<Slot> diskSlots = new ArrayList<>();
    private final TwoWaySyncProperty<Integer> priorityProperty;
    private final TwoWaySyncProperty<FilterMode> filterModeProperty;
    private final TwoWaySyncProperty<Boolean> exactModeProperty;
    private final TwoWaySyncProperty<AccessMode> accessModeProperty;
    private final TwoWaySyncProperty<RedstoneMode> redstoneModeProperty;

    public DiskDriveContainerMenu(int syncId, Inventory playerInventory) {
        super(Rs2Mod.MENUS.getDiskDrive(), syncId);

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

        addDataSlot(priorityProperty);
        addDataSlot(filterModeProperty);
        addDataSlot(exactModeProperty);
        addDataSlot(accessModeProperty);
        addDataSlot(redstoneModeProperty);

        this.storageInfoAccessor = new StorageInfoAccessorImpl(playerInventory.player.getCommandSenderWorld());

        // TODO: add fluid filter slot
        addSlots(playerInventory.player, new SimpleContainer(DiskDriveNetworkNode.DISK_COUNT), new SimpleContainer(9));
    }

    public DiskDriveContainerMenu(int syncId, Player player, SimpleContainer diskInventory, SimpleContainer filterInventory, DiskDriveBlockEntity diskDrive, StorageInfoAccessor storageInfoAccessor) {
        super(Rs2Mod.MENUS.getDiskDrive(), syncId);

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

        addDataSlot(priorityProperty);
        addDataSlot(filterModeProperty);
        addDataSlot(exactModeProperty);
        addDataSlot(accessModeProperty);
        addDataSlot(redstoneModeProperty);

        this.storageInfoAccessor = storageInfoAccessor;

        addSlots(player, diskInventory, filterInventory);
    }

    private void addSlots(Player player, SimpleContainer diskInventory, SimpleContainer filterInventory) {
        for (int i = 0; i < DiskDriveNetworkNode.DISK_COUNT; ++i) {
            diskSlots.add(addSlot(createDiskSlot(diskInventory, i)));
        }
        for (int i = 0; i < 9; ++i) {
            addSlot(createFilterSlot(player, filterInventory, i));
        }
        addPlayerInventory(player.getInventory(), 8, 141);
    }

    private FilterSlot createFilterSlot(Player player, SimpleContainer filterInventory, int i) {
        int x = FILTER_SLOT_X + (18 * i);
        return new FilterSlot(filterInventory, i, x, FILTER_SLOT_Y);
    }

    private Slot createDiskSlot(SimpleContainer diskInventory, int i) {
        int x = DISK_SLOT_X + ((i % 2) * 18);
        int y = DISK_SLOT_Y + Math.floorDiv(i, 2) * 18;
        return new ValidatedSlot(diskInventory, i, x, y, stack -> stack.getItem() instanceof StorageDiskItem);
    }

    public boolean hasInfiniteDisk() {
        return getStorageDiskInfo().anyMatch(info -> info.capacity() == -1);
    }

    public double getProgress() {
        if (hasInfiniteDisk()) {
            return 0;
        }
        return (double) getStored() / (double) getCapacity();
    }

    public long getCapacity() {
        return getStorageDiskInfo().mapToLong(StorageInfo::capacity).sum();
    }

    public long getStored() {
        return getStorageDiskInfo().mapToLong(StorageInfo::stored).sum();
    }

    private Stream<StorageInfo> getStorageDiskInfo() {
        return diskSlots
                .stream()
                .map(Slot::getItem)
                .filter(stack -> !stack.isEmpty())
                .map(storageInfoAccessor::getInfo)
                .flatMap(info -> info.map(Stream::of).orElseGet(Stream::empty));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack originalStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            originalStack = stackInSlot.copy();

            if (index < 8) {
                if (!moveItemStackTo(stackInSlot, 8, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stackInSlot, 0, 8, false)) {
                return ItemStack.EMPTY;
            }

            if (stackInSlot.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
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
