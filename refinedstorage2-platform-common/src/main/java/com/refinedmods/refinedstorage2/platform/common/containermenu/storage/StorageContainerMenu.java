package com.refinedmods.refinedstorage2.platform.common.containermenu.storage;

import com.refinedmods.refinedstorage2.api.core.filter.FilterMode;
import com.refinedmods.refinedstorage2.api.storage.AccessMode;
import com.refinedmods.refinedstorage2.platform.api.network.node.RedstoneMode;
import com.refinedmods.refinedstorage2.platform.common.block.entity.AccessModeSettings;
import com.refinedmods.refinedstorage2.platform.common.block.entity.FilterModeSettings;
import com.refinedmods.refinedstorage2.platform.common.block.entity.RedstoneModeSettings;
import com.refinedmods.refinedstorage2.platform.common.containermenu.ResourceFilterableContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.TwoWaySyncProperty;

import net.minecraft.world.inventory.MenuType;

public abstract class StorageContainerMenu extends ResourceFilterableContainerMenu implements StorageAccessor {
    private final TwoWaySyncProperty<Integer> priorityProperty;
    private final TwoWaySyncProperty<FilterMode> filterModeProperty;
    private final TwoWaySyncProperty<Boolean> exactModeProperty;
    private final TwoWaySyncProperty<AccessMode> accessModeProperty;
    private final TwoWaySyncProperty<RedstoneMode> redstoneModeProperty;

    protected StorageContainerMenu(MenuType<?> type, int syncId) {
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
    }

    protected StorageContainerMenu(MenuType<?> type, int syncId, StorageSettingsProvider storageSettingsProvider) {
        super(type, syncId);

        this.priorityProperty = TwoWaySyncProperty.forServer(
                0,
                priority -> priority,
                priority -> priority,
                storageSettingsProvider::getPriority,
                storageSettingsProvider::setPriority
        );
        this.filterModeProperty = TwoWaySyncProperty.forServer(
                1,
                FilterModeSettings::getFilterMode,
                FilterModeSettings::getFilterMode,
                storageSettingsProvider::getFilterMode,
                storageSettingsProvider::setFilterMode
        );
        this.exactModeProperty = TwoWaySyncProperty.forServer(
                2,
                value -> Boolean.TRUE.equals(value) ? 0 : 1,
                value -> value == 0,
                storageSettingsProvider::isExactMode,
                storageSettingsProvider::setExactMode
        );
        this.accessModeProperty = TwoWaySyncProperty.forServer(
                3,
                AccessModeSettings::getAccessMode,
                AccessModeSettings::getAccessMode,
                storageSettingsProvider::getAccessMode,
                storageSettingsProvider::setAccessMode
        );
        this.redstoneModeProperty = TwoWaySyncProperty.forServer(
                4,
                RedstoneModeSettings::getRedstoneMode,
                RedstoneModeSettings::getRedstoneMode,
                storageSettingsProvider::getRedstoneMode,
                storageSettingsProvider::setRedstoneMode
        );

        addDataSlot(priorityProperty);
        addDataSlot(filterModeProperty);
        addDataSlot(exactModeProperty);
        addDataSlot(accessModeProperty);
        addDataSlot(redstoneModeProperty);
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