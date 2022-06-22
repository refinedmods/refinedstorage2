package com.refinedmods.refinedstorage2.platform.common.containermenu.storage.block;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.ResourceType;
import com.refinedmods.refinedstorage2.platform.apiimpl.resource.filter.FilteredResourceFilterContainer;
import com.refinedmods.refinedstorage2.platform.apiimpl.resource.filter.ResourceFilterContainer;
import com.refinedmods.refinedstorage2.platform.common.block.entity.storage.StorageBlockBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.containermenu.slot.ResourceFilterSlot;
import com.refinedmods.refinedstorage2.platform.common.containermenu.storage.StorageContainerMenu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;

public abstract class StorageBlockContainerMenu<T> extends StorageContainerMenu {
    private static final int FILTER_SLOT_X = 8;
    private static final int FILTER_SLOT_Y = 20;

    private long stored;
    private long capacity;

    protected StorageBlockContainerMenu(MenuType<?> type, int syncId, Player player, FriendlyByteBuf buf, ResourceType<T> resourceType) {
        super(type, syncId);

        this.stored = buf.readLong();
        this.capacity = buf.readLong();

        addSlots(player, new FilteredResourceFilterContainer(PlatformApi.INSTANCE.getResourceTypeRegistry(), 9, () -> {
        }, resourceType));

        initializeResourceFilterSlots(buf);
    }

    protected StorageBlockContainerMenu(MenuType<?> type, int syncId, Player player, ResourceFilterContainer resourceFilterContainer, StorageBlockBlockEntity<?> storageBlock) {
        super(type, syncId, player, storageBlock, resourceFilterContainer);
        addSlots(player, resourceFilterContainer);
    }

    private void addSlots(Player player, ResourceFilterContainer resourceFilterContainer) {
        for (int i = 0; i < 9; ++i) {
            addSlot(createFilterSlot(resourceFilterContainer, i));
        }
        addPlayerInventory(player.getInventory(), 8, 141);
    }

    private Slot createFilterSlot(ResourceFilterContainer resourceFilterContainer, int i) {
        int x = FILTER_SLOT_X + (18 * i);
        return new ResourceFilterSlot(resourceFilterContainer, i, x, FILTER_SLOT_Y);
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
