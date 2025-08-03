package com.refinedmods.refinedstorage.common.storage.diskdrive;

import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.storage.StorageContainerItem;
import com.refinedmods.refinedstorage.common.api.storage.StorageInfo;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage.common.content.Menus;
import com.refinedmods.refinedstorage.common.storage.AbstractStorageContainerMenu;
import com.refinedmods.refinedstorage.common.storage.StorageAccessor;
import com.refinedmods.refinedstorage.common.storage.StorageConfigurationContainer;
import com.refinedmods.refinedstorage.common.support.FilteredContainer;
import com.refinedmods.refinedstorage.common.support.containermenu.ResourceSlot;
import com.refinedmods.refinedstorage.common.support.containermenu.ResourceSlotType;
import com.refinedmods.refinedstorage.common.support.containermenu.ValidatedSlot;
import com.refinedmods.refinedstorage.common.support.resource.ResourceContainerData;
import com.refinedmods.refinedstorage.common.support.resource.ResourceContainerImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public class DiskDriveContainerMenu extends AbstractStorageContainerMenu implements StorageAccessor {
    private static final int DISK_SLOT_X = 61;
    private static final int DISK_SLOT_Y = 54;

    private static final int FILTER_SLOT_X = 8;
    private static final int FILTER_SLOT_Y = 20;

    private final StorageDiskInfoAccessor storageInfoAccessor;
    private final List<Slot> diskSlots = new ArrayList<>();
    private final Predicate<Player> stillValid;

    public DiskDriveContainerMenu(final int syncId,
                                  final Inventory playerInventory,
                                  final ResourceContainerData resourceContainerData) {
        super(Menus.INSTANCE.getDiskDrive(), syncId);
        this.storageInfoAccessor = new StorageDiskInfoAccessorImpl(
            RefinedStorageApi.INSTANCE.getClientStorageRepository()
        );
        addSlots(
            playerInventory.player,
            new FilteredContainer(
                AbstractDiskDriveBlockEntity.AMOUNT_OF_DISKS,
                StorageContainerItem.stackValidator()
            ),
            ResourceContainerImpl.createForFilter(resourceContainerData)
        );
        this.stillValid = p -> true;
    }

    DiskDriveContainerMenu(final int syncId,
                           final Player player,
                           final FilteredContainer diskInventory,
                           final ResourceContainer filterContainer,
                           final StorageConfigurationContainer configContainer,
                           final StorageDiskInfoAccessor storageInfoAccessor,
                           final Predicate<Player> stillValid) {
        super(Menus.INSTANCE.getDiskDrive(), syncId, player, configContainer);
        this.storageInfoAccessor = storageInfoAccessor;
        addSlots(player, diskInventory, filterContainer);
        this.stillValid = stillValid;
    }

    private void addSlots(final Player player,
                          final FilteredContainer diskInventory,
                          final ResourceContainer filterContainer) {
        for (int i = 0; i < diskInventory.getContainerSize(); ++i) {
            diskSlots.add(addSlot(createDiskSlot(diskInventory, i)));
        }
        for (int i = 0; i < filterContainer.size(); ++i) {
            addSlot(createFilterSlot(filterContainer, i));
        }
        addPlayerInventory(player.getInventory(), 8, 141);

        transferManager.addBiTransfer(player.getInventory(), diskInventory);
        transferManager.addFilterTransfer(player.getInventory());
    }

    private Slot createFilterSlot(final ResourceContainer filterContainer, final int i) {
        final int x = FILTER_SLOT_X + (18 * i);
        return new ResourceSlot(
            filterContainer,
            i,
            createTranslation("gui", "storage.filter_help"),
            x,
            FILTER_SLOT_Y,
            ResourceSlotType.FILTER
        );
    }

    private Slot createDiskSlot(final FilteredContainer diskInventory, final int i) {
        final int x = DISK_SLOT_X + ((i % 2) * 18);
        final int y = DISK_SLOT_Y + Math.floorDiv(i, 2) * 18;
        return ValidatedSlot.forStorageContainer(diskInventory, i, x, y);
    }

    @Override
    public boolean hasCapacity() {
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

    @Override
    public boolean stillValid(final Player player) {
        return stillValid.test(player);
    }
}
