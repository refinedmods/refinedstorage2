package com.refinedmods.refinedstorage.platform.common.storage.storageblock;

import com.refinedmods.refinedstorage.platform.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage.platform.api.support.resource.ResourceFactory;
import com.refinedmods.refinedstorage.platform.common.storage.AbstractStorageContainerMenu;
import com.refinedmods.refinedstorage.platform.common.storage.StorageAccessor;
import com.refinedmods.refinedstorage.platform.common.storage.StorageConfigurationContainer;
import com.refinedmods.refinedstorage.platform.common.support.containermenu.ResourceSlot;
import com.refinedmods.refinedstorage.platform.common.support.containermenu.ResourceSlotType;
import com.refinedmods.refinedstorage.platform.common.support.resource.ResourceContainerImpl;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createTranslation;

public abstract class AbstractStorageBlockContainerMenu extends AbstractStorageContainerMenu
    implements StorageAccessor {
    private static final int FILTER_SLOT_X = 8;
    private static final int FILTER_SLOT_Y = 20;

    private long stored;
    private long capacity;

    protected AbstractStorageBlockContainerMenu(final MenuType<?> type,
                                                final int syncId,
                                                final Player player,
                                                final StorageBlockData storageBlockData,
                                                final ResourceFactory resourceFactory) {
        super(type, syncId);
        this.stored = storageBlockData.stored();
        this.capacity = storageBlockData.capacity();
        addSlots(
            player,
            ResourceContainerImpl.createForFilter(resourceFactory, storageBlockData.resourceContainerData())
        );
    }

    protected AbstractStorageBlockContainerMenu(final MenuType<?> type,
                                                final int syncId,
                                                final Player player,
                                                final ResourceContainer resourceContainer,
                                                final StorageConfigurationContainer configContainer) {
        super(type, syncId, player, configContainer);
        addSlots(player, resourceContainer);
    }

    private void addSlots(final Player player, final ResourceContainer resourceContainer) {
        for (int i = 0; i < resourceContainer.size(); ++i) {
            addSlot(createFilterSlot(resourceContainer, i));
        }
        addPlayerInventory(player.getInventory(), 8, 141);

        transferManager.addFilterTransfer(player.getInventory());
    }

    private Slot createFilterSlot(final ResourceContainer resourceContainer, final int i) {
        final int x = FILTER_SLOT_X + (18 * i);
        return new ResourceSlot(
            resourceContainer,
            i,
            createTranslation("gui", "storage.filter_help"),
            x,
            FILTER_SLOT_Y,
            ResourceSlotType.FILTER
        );
    }

    @Override
    public double getProgress() {
        if (capacity == 0) {
            return 0;
        }
        return (double) getStored() / (double) getCapacity();
    }

    @Override
    public long getCapacity() {
        return capacity;
    }

    @Override
    public long getStored() {
        return stored;
    }
}
