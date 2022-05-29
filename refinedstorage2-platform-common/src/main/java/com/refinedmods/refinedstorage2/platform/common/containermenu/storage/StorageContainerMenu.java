package com.refinedmods.refinedstorage2.platform.common.containermenu.storage;

import com.refinedmods.refinedstorage2.api.core.filter.FilterMode;
import com.refinedmods.refinedstorage2.api.storage.AccessMode;
import com.refinedmods.refinedstorage2.platform.api.network.node.RedstoneMode;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.FilteredResourceFilterContainer;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.ResourceFilterContainer;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.ResourceType;
import com.refinedmods.refinedstorage2.platform.common.block.entity.AccessModeSettings;
import com.refinedmods.refinedstorage2.platform.common.block.entity.FilterModeSettings;
import com.refinedmods.refinedstorage2.platform.common.block.entity.RedstoneModeSettings;
import com.refinedmods.refinedstorage2.platform.common.block.entity.storage.StorageBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.containermenu.ResourceFilterableContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.StorageAccessor;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.TwoWaySyncProperty;
import com.refinedmods.refinedstorage2.platform.common.containermenu.slot.ResourceFilterSlot;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public abstract class StorageContainerMenu<T> extends ResourceFilterableContainerMenu implements StorageAccessor {
    private static final int FILTER_SLOT_X = 8;
    private static final int FILTER_SLOT_Y = 20;

    private final TwoWaySyncProperty<Integer> priorityProperty;
    private final TwoWaySyncProperty<FilterMode> filterModeProperty;
    private final TwoWaySyncProperty<Boolean> exactModeProperty;
    private final TwoWaySyncProperty<AccessMode> accessModeProperty;
    private final TwoWaySyncProperty<RedstoneMode> redstoneModeProperty;

    private long stored;
    private long capacity;

    protected StorageContainerMenu(MenuType<?> type, int syncId, Inventory playerInventory, FriendlyByteBuf buf, ResourceType<T> resourceType) {
        super(type, syncId);

        this.priorityProperty = TwoWaySyncProperty.integerForClient(0);
        this.filterModeProperty = FilterModeSettings.createClientSyncProperty(1);
        this.exactModeProperty = TwoWaySyncProperty.booleanForClient(2);
        this.accessModeProperty = AccessModeSettings.createClientSyncProperty(3);
        this.redstoneModeProperty = RedstoneModeSettings.createClientSyncProperty(4);

        addDataSlot(priorityProperty);
        addDataSlot(filterModeProperty);
        addDataSlot(exactModeProperty);
        addDataSlot(accessModeProperty);
        addDataSlot(redstoneModeProperty);

        stored = buf.readLong();
        capacity = buf.readLong();

        addSlots(playerInventory.player, new FilteredResourceFilterContainer(9, () -> {
        }, resourceType));

        initializeResourceFilterSlots(buf);
    }

    protected StorageContainerMenu(MenuType<?> type,
                                   int syncId,
                                   Player player,
                                   ResourceFilterContainer resourceFilterContainer,
                                   StorageBlockEntity<?> storageBlock) {
        super(type, syncId, player, resourceFilterContainer);

        this.priorityProperty = TwoWaySyncProperty.forServer(
                0,
                priority -> priority,
                priority -> priority,
                storageBlock::getPriority,
                storageBlock::setPriority
        );
        this.filterModeProperty = TwoWaySyncProperty.forServer(
                1,
                FilterModeSettings::getFilterMode,
                FilterModeSettings::getFilterMode,
                storageBlock::getFilterMode,
                storageBlock::setFilterMode
        );
        this.exactModeProperty = TwoWaySyncProperty.forServer(
                2,
                value -> Boolean.TRUE.equals(value) ? 0 : 1,
                value -> value == 0,
                storageBlock::isExactMode,
                storageBlock::setExactMode
        );
        this.accessModeProperty = TwoWaySyncProperty.forServer(
                3,
                AccessModeSettings::getAccessMode,
                AccessModeSettings::getAccessMode,
                storageBlock::getAccessMode,
                storageBlock::setAccessMode
        );
        this.redstoneModeProperty = TwoWaySyncProperty.forServer(
                4,
                RedstoneModeSettings::getRedstoneMode,
                RedstoneModeSettings::getRedstoneMode,
                storageBlock::getRedstoneMode,
                storageBlock::setRedstoneMode
        );

        addDataSlot(priorityProperty);
        addDataSlot(filterModeProperty);
        addDataSlot(exactModeProperty);
        addDataSlot(accessModeProperty);
        addDataSlot(redstoneModeProperty);

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
    public boolean hasCapacity() {
        return capacity > 0;
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
        return capacity;
    }

    @Override
    public long getStored() {
        return stored;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack originalStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
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
