package com.refinedmods.refinedstorage.platform.common.storage.diskinterface;

import com.refinedmods.refinedstorage.api.network.impl.node.storagetransfer.StorageTransferMode;
import com.refinedmods.refinedstorage.api.resource.filter.FilterMode;
import com.refinedmods.refinedstorage.platform.api.PlatformApi;
import com.refinedmods.refinedstorage.platform.api.storage.StorageContainerItem;
import com.refinedmods.refinedstorage.platform.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage.platform.common.content.Menus;
import com.refinedmods.refinedstorage.platform.common.storage.DiskInventory;
import com.refinedmods.refinedstorage.platform.common.support.FilteredContainer;
import com.refinedmods.refinedstorage.platform.common.support.RedstoneMode;
import com.refinedmods.refinedstorage.platform.common.support.containermenu.AbstractResourceContainerMenu;
import com.refinedmods.refinedstorage.platform.common.support.containermenu.ClientProperty;
import com.refinedmods.refinedstorage.platform.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage.platform.common.support.containermenu.ResourceSlot;
import com.refinedmods.refinedstorage.platform.common.support.containermenu.ResourceSlotType;
import com.refinedmods.refinedstorage.platform.common.support.containermenu.ServerProperty;
import com.refinedmods.refinedstorage.platform.common.support.containermenu.ValidatedSlot;
import com.refinedmods.refinedstorage.platform.common.support.resource.ResourceContainerData;
import com.refinedmods.refinedstorage.platform.common.support.resource.ResourceContainerImpl;
import com.refinedmods.refinedstorage.platform.common.upgrade.UpgradeContainer;
import com.refinedmods.refinedstorage.platform.common.upgrade.UpgradeDestinations;
import com.refinedmods.refinedstorage.platform.common.upgrade.UpgradeSlot;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createTranslation;

public class DiskInterfaceContainerMenu extends AbstractResourceContainerMenu {
    private static final int DISK_SLOT_X1 = 44;
    private static final int DISK_SLOT_X2 = 116;
    private static final int DISK_SLOT_Y = 57;

    private static final int FILTER_SLOT_X = 8;
    private static final int FILTER_SLOT_Y = 20;

    DiskInterfaceContainerMenu(final int syncId,
                               final Player player,
                               final AbstractDiskInterfaceBlockEntity blockEntity,
                               final DiskInventory diskInventory,
                               final ResourceContainer filterContainer,
                               final UpgradeContainer upgradeContainer) {
        super(Menus.INSTANCE.getDiskInterface(), syncId, player);
        addSlots(player, diskInventory, filterContainer, upgradeContainer);
        registerProperty(new ServerProperty<>(
            PropertyTypes.REDSTONE_MODE,
            blockEntity::getRedstoneMode,
            blockEntity::setRedstoneMode
        ));
        registerProperty(new ServerProperty<>(
            PropertyTypes.FUZZY_MODE,
            blockEntity::isFuzzyMode,
            blockEntity::setFuzzyMode
        ));
        registerProperty(new ServerProperty<>(
            PropertyTypes.FILTER_MODE,
            blockEntity::getFilterMode,
            blockEntity::setFilterMode
        ));
        registerProperty(new ServerProperty<>(
            DiskInterfacePropertyTypes.TRANSFER_MODE,
            blockEntity::getTransferMode,
            blockEntity::setTransferMode
        ));
    }

    public DiskInterfaceContainerMenu(final int syncId,
                                      final Inventory playerInventory,
                                      final ResourceContainerData resourceContainerData) {
        super(Menus.INSTANCE.getDiskInterface(), syncId);
        addSlots(
            playerInventory.player,
            new FilteredContainer(
                AbstractDiskInterfaceBlockEntity.AMOUNT_OF_DISKS,
                StorageContainerItem.stackValidator()
            ),
            ResourceContainerImpl.createForFilter(resourceContainerData),
            new UpgradeContainer(UpgradeDestinations.DISK_INTERFACE, PlatformApi.INSTANCE.getUpgradeRegistry())
        );
        registerProperty(new ClientProperty<>(PropertyTypes.REDSTONE_MODE, RedstoneMode.IGNORE));
        registerProperty(new ClientProperty<>(PropertyTypes.FUZZY_MODE, false));
        registerProperty(new ClientProperty<>(PropertyTypes.FILTER_MODE, FilterMode.BLOCK));
        registerProperty(new ClientProperty<>(
            DiskInterfacePropertyTypes.TRANSFER_MODE,
            StorageTransferMode.INSERT_INTO_NETWORK
        ));
    }

    private void addSlots(final Player player,
                          final FilteredContainer diskInventory,
                          final ResourceContainer filterContainer,
                          final UpgradeContainer upgradeContainer) {
        for (int i = 0; i < diskInventory.getContainerSize(); ++i) {
            addSlot(createDiskSlot(diskInventory, i));
        }
        for (int i = 0; i < filterContainer.size(); ++i) {
            addSlot(createFilterSlot(filterContainer, i));
        }
        for (int i = 0; i < upgradeContainer.getContainerSize(); ++i) {
            addSlot(new UpgradeSlot(upgradeContainer, i, 187, 6 + (i * 18)));
        }
        addPlayerInventory(player.getInventory(), 8, 129);

        transferManager.addBiTransfer(player.getInventory(), upgradeContainer);
        transferManager.addBiTransfer(player.getInventory(), diskInventory);
        transferManager.addFilterTransfer(player.getInventory());
    }

    private Slot createFilterSlot(final ResourceContainer filterContainer, final int i) {
        final int x = FILTER_SLOT_X + (18 * i);
        return new ResourceSlot(
            filterContainer,
            i,
            createTranslation("gui", "disk_interface.filter_help"),
            x,
            FILTER_SLOT_Y,
            ResourceSlotType.FILTER
        );
    }

    private Slot createDiskSlot(final FilteredContainer diskInventory, final int i) {
        final int x = i < 3 ? DISK_SLOT_X1 : DISK_SLOT_X2;
        final int y = DISK_SLOT_Y + ((i % 3) * 18);
        return ValidatedSlot.forStorageContainer(diskInventory, i, x, y);
    }
}
