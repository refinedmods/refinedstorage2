package com.refinedmods.refinedstorage2.platform.common.containermenu.property;

import com.refinedmods.refinedstorage2.api.core.filter.FilterMode;
import com.refinedmods.refinedstorage2.api.storage.AccessMode;
import com.refinedmods.refinedstorage2.platform.common.block.entity.AccessModeSettings;
import com.refinedmods.refinedstorage2.platform.common.block.entity.FilterModeSettings;
import com.refinedmods.refinedstorage2.platform.common.block.entity.RedstoneModeSettings;
import com.refinedmods.refinedstorage2.platform.common.block.entity.exporter.ExporterSchedulingModeSettings;
import com.refinedmods.refinedstorage2.platform.common.util.RedstoneMode;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;

public final class PropertyTypes {
    public static final PropertyType<RedstoneMode> REDSTONE_MODE = new PropertyType<>(
        createIdentifier("redstone_mode"),
        RedstoneModeSettings::getRedstoneMode,
        RedstoneModeSettings::getRedstoneMode
    );

    public static final PropertyType<FilterMode> FILTER_MODE = new PropertyType<>(
        createIdentifier("filter_mode"),
        FilterModeSettings::getFilterMode,
        FilterModeSettings::getFilterMode
    );

    public static final PropertyType<Integer> PRIORITY = new PropertyType<>(
        createIdentifier("priority"),
        value -> value,
        value -> value
    );

    public static final PropertyType<AccessMode> ACCESS_MODE = new PropertyType<>(
        createIdentifier("access_mode"),
        AccessModeSettings::getAccessMode,
        AccessModeSettings::getAccessMode
    );

    public static final PropertyType<ExporterSchedulingModeSettings> EXPORTER_SCHEDULING_MODE = new PropertyType<>(
        createIdentifier("exporter_scheduling_mode"),
        ExporterSchedulingModeSettings::getId,
        ExporterSchedulingModeSettings::getById
    );

    public static final PropertyType<Boolean> FUZZY_MODE = new PropertyType<>(
        createIdentifier("fuzzy_mode"),
        value -> Boolean.TRUE.equals(value) ? 1 : 0,
        value -> value == 1
    );

    private PropertyTypes() {
    }
}
