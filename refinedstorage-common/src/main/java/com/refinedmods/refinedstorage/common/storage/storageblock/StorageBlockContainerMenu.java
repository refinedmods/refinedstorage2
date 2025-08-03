package com.refinedmods.refinedstorage.common.storage.storageblock;

import com.refinedmods.refinedstorage.common.api.storage.StorageBlockData;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceFactory;
import com.refinedmods.refinedstorage.common.storage.AbstractStorageContainerMenu;
import com.refinedmods.refinedstorage.common.storage.StorageAccessor;
import com.refinedmods.refinedstorage.common.storage.StorageConfigurationContainer;
import com.refinedmods.refinedstorage.common.support.containermenu.ResourceSlot;
import com.refinedmods.refinedstorage.common.support.containermenu.ResourceSlotType;
import com.refinedmods.refinedstorage.common.support.resource.ResourceContainerImpl;

import java.util.function.Predicate;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public class StorageBlockContainerMenu extends AbstractStorageContainerMenu implements StorageAccessor {
    private static final int FILTER_SLOT_X = 8;
    private static final int FILTER_SLOT_Y = 20;

    private final Predicate<Player> stillValid;
    private long stored;
    private long capacity;

    public StorageBlockContainerMenu(final MenuType<?> type,
                                     final int syncId,
                                     final Player player,
                                     final StorageBlockData storageBlockData,
                                     final ResourceFactory resourceFactory) {
        super(type, syncId);
        this.stored = storageBlockData.stored();
        this.capacity = storageBlockData.capacity();
        addSlots(
            player,
            ResourceContainerImpl.createForFilter(resourceFactory, storageBlockData.resources())
        );
        this.stillValid = p -> true;
    }

    public StorageBlockContainerMenu(final MenuType<?> type,
                                     final int syncId,
                                     final Player player,
                                     final ResourceContainer resourceContainer,
                                     final StorageConfigurationContainer configContainer,
                                     final Predicate<Player> stillValid) {
        super(type, syncId, player, configContainer);
        addSlots(player, resourceContainer);
        this.stillValid = stillValid;
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
    public boolean hasCapacity() {
        return capacity > 0;
    }

    @Override
    public long getCapacity() {
        return capacity;
    }

    @Override
    public long getStored() {
        return stored;
    }

    @Override
    public boolean stillValid(final Player player) {
        return stillValid.test(player);
    }
}
