package com.refinedmods.refinedstorage2.platform.common.block.entity;

import com.refinedmods.refinedstorage2.api.network.node.StorageConfiguration;

import net.minecraft.nbt.CompoundTag;

public final class StorageConfigurationPersistence {
    private static final String TAG_PRIORITY = "pri";
    private static final String TAG_FILTER_MODE = "fim";
    private static final String TAG_ACCESS_MODE = "am";

    private final StorageConfiguration storageConfiguration;

    public StorageConfigurationPersistence(final StorageConfiguration storageConfiguration) {
        this.storageConfiguration = storageConfiguration;
    }

    public void load(final CompoundTag tag) {
        if (tag.contains(TAG_PRIORITY)) {
            storageConfiguration.setPriority(tag.getInt(TAG_PRIORITY));
        }
        if (tag.contains(TAG_FILTER_MODE)) {
            storageConfiguration.setFilterMode(FilterModeSettings.getFilterMode(tag.getInt(TAG_FILTER_MODE)));
        }
        if (tag.contains(TAG_ACCESS_MODE)) {
            storageConfiguration.setAccessMode(AccessModeSettings.getAccessMode(tag.getInt(TAG_ACCESS_MODE)));
        }
    }

    public void save(final CompoundTag tag) {
        tag.putInt(TAG_FILTER_MODE, FilterModeSettings.getFilterMode(storageConfiguration.getFilterMode()));
        tag.putInt(TAG_PRIORITY, storageConfiguration.getPriority());
        tag.putInt(TAG_ACCESS_MODE, AccessModeSettings.getAccessMode(storageConfiguration.getAccessMode()));
    }
}
