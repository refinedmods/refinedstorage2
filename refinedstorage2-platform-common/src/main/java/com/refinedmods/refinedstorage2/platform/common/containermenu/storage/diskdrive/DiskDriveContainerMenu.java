package com.refinedmods.refinedstorage2.platform.common.containermenu.storage.diskdrive;

import com.refinedmods.refinedstorage2.api.storage.StorageInfo;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.storage.StorageTooltipHelper;
import com.refinedmods.refinedstorage2.platform.api.storage.item.StorageDiskItem;
import com.refinedmods.refinedstorage2.platform.common.block.entity.diskdrive.AbstractDiskDriveBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.containermenu.slot.ResourceFilterSlot;
import com.refinedmods.refinedstorage2.platform.common.containermenu.slot.ValidatedSlot;
import com.refinedmods.refinedstorage2.platform.common.containermenu.storage.AbstractStorageContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.content.Menus;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.filter.ResourceFilterContainer;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class DiskDriveContainerMenu extends AbstractStorageContainerMenu {
    private static final int DISK_SLOT_X = 61;
    private static final int DISK_SLOT_Y = 54;

    private static final int FILTER_SLOT_X = 8;
    private static final int FILTER_SLOT_Y = 20;

    private final StorageDiskInfoAccessor storageInfoAccessor;

    private final List<Slot> diskSlots = new ArrayList<>();

    public DiskDriveContainerMenu(final int syncId, final Inventory playerInventory, final FriendlyByteBuf buf) {
        super(Menus.INSTANCE.getDiskDrive(), syncId, PlatformApi.INSTANCE.getResourceTypeRegistry());

        this.storageInfoAccessor = new StorageDiskInfoAccessorImpl(playerInventory.player.getCommandSenderWorld());

        addSlots(
            playerInventory.player,
            new SimpleContainer(9),
            new ResourceFilterContainer(PlatformApi.INSTANCE.getResourceTypeRegistry(), 9, false)
        );

        initializeResourceFilterSlots(buf);
    }

    public DiskDriveContainerMenu(final int syncId,
                                  final Player player,
                                  final SimpleContainer diskInventory,
                                  final ResourceFilterContainer resourceFilterContainer,
                                  final AbstractDiskDriveBlockEntity diskDrive,
                                  final StorageDiskInfoAccessor storageInfoAccessor) {
        super(
            Menus.INSTANCE.getDiskDrive(),
            syncId,
            PlatformApi.INSTANCE.getResourceTypeRegistry(),
            player,
            diskDrive,
            resourceFilterContainer
        );
        this.storageInfoAccessor = storageInfoAccessor;
        addSlots(player, diskInventory, resourceFilterContainer);
    }

    private void addSlots(final Player player,
                          final SimpleContainer diskInventory,
                          final ResourceFilterContainer resourceFilterContainer) {
        for (int i = 0; i < diskInventory.getContainerSize(); ++i) {
            diskSlots.add(addSlot(createDiskSlot(diskInventory, i)));
        }
        for (int i = 0; i < resourceFilterContainer.size(); ++i) {
            addSlot(createFilterSlot(resourceFilterContainer, i));
        }
        addPlayerInventory(player.getInventory(), 8, 141);

        transferManager.addBiTransfer(player.getInventory(), diskInventory);
        transferManager.addFilterTransfer(player.getInventory());
    }

    private Slot createFilterSlot(final ResourceFilterContainer resourceFilterContainer, final int i) {
        final int x = FILTER_SLOT_X + (18 * i);
        return new ResourceFilterSlot(resourceFilterContainer, i, x, FILTER_SLOT_Y);
    }

    private Slot createDiskSlot(final SimpleContainer diskInventory, final int i) {
        final int x = DISK_SLOT_X + ((i % 2) * 18);
        final int y = DISK_SLOT_Y + Math.floorDiv(i, 2) * 18;
        return new ValidatedSlot(diskInventory, i, x, y, stack -> stack.getItem() instanceof StorageDiskItem);
    }

    private boolean hasCapacity() {
        return getStorageDiskInfo().allMatch(info -> info.capacity() > 0);
    }

    @Override
    public double getProgress() {
        if (!hasCapacity()) {
            return 0;
        }
        return (double) getStored() / (double) getCapacity();
    }

    @Override
    public Set<StorageTooltipHelper.TooltipOption> getTooltipOptions() {
        final Set<StorageTooltipHelper.TooltipOption> options =
            EnumSet.noneOf(StorageTooltipHelper.TooltipOption.class);
        if (hasCapacity()) {
            options.add(StorageTooltipHelper.TooltipOption.CAPACITY_AND_PROGRESS);
        }
        if (getDiskStacks().allMatch(storageInfoAccessor::hasStacking)) {
            options.add(StorageTooltipHelper.TooltipOption.STACK_INFO);
        }
        return options;
    }

    @Override
    public long getCapacity() {
        return getStorageDiskInfo().mapToLong(StorageInfo::capacity).sum();
    }

    @Override
    public long getStored() {
        return getStorageDiskInfo().mapToLong(StorageInfo::stored).sum();
    }

    private Stream<ItemStack> getDiskStacks() {
        return diskSlots
            .stream()
            .map(Slot::getItem)
            .filter(stack -> !stack.isEmpty());
    }

    private Stream<StorageInfo> getStorageDiskInfo() {
        return getDiskStacks()
            .map(storageInfoAccessor::getInfo)
            .flatMap(Optional::stream);
    }
}
