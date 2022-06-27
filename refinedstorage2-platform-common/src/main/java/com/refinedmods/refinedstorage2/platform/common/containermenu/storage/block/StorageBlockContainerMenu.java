package com.refinedmods.refinedstorage2.platform.common.containermenu.storage.block;

import com.refinedmods.refinedstorage2.api.core.registry.OrderedRegistry;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.ResourceType;
import com.refinedmods.refinedstorage2.platform.apiimpl.resource.filter.FilteredResourceFilterContainer;
import com.refinedmods.refinedstorage2.platform.apiimpl.resource.filter.ResourceFilterContainer;
import com.refinedmods.refinedstorage2.platform.common.block.entity.storage.StorageBlockBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.containermenu.slot.ResourceFilterSlot;
import com.refinedmods.refinedstorage2.platform.common.containermenu.storage.StorageContainerMenu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;

public abstract class StorageBlockContainerMenu extends StorageContainerMenu {
    private static final int FILTER_SLOT_X = 8;
    private static final int FILTER_SLOT_Y = 20;

    private long stored;
    private long capacity;

    protected StorageBlockContainerMenu(final MenuType<?> type, final int syncId, final OrderedRegistry<ResourceLocation, ResourceType> resourceTypeRegistry, final Player player, final FriendlyByteBuf buf, final ResourceType resourceType) {
        super(type, syncId, resourceTypeRegistry);

        this.stored = buf.readLong();
        this.capacity = buf.readLong();

        addSlots(player, new FilteredResourceFilterContainer(PlatformApi.INSTANCE.getResourceTypeRegistry(), 9, () -> {
        }, resourceType));

        initializeResourceFilterSlots(buf);
    }

    protected StorageBlockContainerMenu(final MenuType<?> type, final int syncId, final OrderedRegistry<ResourceLocation, ResourceType> resourceTypeRegistry, final Player player, final ResourceFilterContainer resourceFilterContainer, final StorageBlockBlockEntity<?> storageBlock) {
        super(type, syncId, resourceTypeRegistry, player, storageBlock, resourceFilterContainer);
        addSlots(player, resourceFilterContainer);
    }

    private void addSlots(final Player player, final ResourceFilterContainer resourceFilterContainer) {
        for (int i = 0; i < 9; ++i) {
            addSlot(createFilterSlot(resourceFilterContainer, i));
        }
        addPlayerInventory(player.getInventory(), 8, 141);
    }

    private Slot createFilterSlot(final ResourceFilterContainer resourceFilterContainer, final int i) {
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
