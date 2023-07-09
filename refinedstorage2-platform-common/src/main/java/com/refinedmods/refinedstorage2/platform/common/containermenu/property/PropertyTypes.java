package com.refinedmods.refinedstorage2.platform.common.containermenu.property;

import com.refinedmods.refinedstorage2.api.core.filter.FilterMode;
import com.refinedmods.refinedstorage2.api.network.impl.node.detector.DetectorMode;
import com.refinedmods.refinedstorage2.api.storage.AccessMode;
import com.refinedmods.refinedstorage2.platform.common.block.entity.AccessModeSettings;
import com.refinedmods.refinedstorage2.platform.common.block.entity.FilterModeSettings;
import com.refinedmods.refinedstorage2.platform.common.block.entity.RedstoneModeSettings;
import com.refinedmods.refinedstorage2.platform.common.block.entity.SchedulingModeType;
import com.refinedmods.refinedstorage2.platform.common.block.entity.detector.DetectorModeSettings;
import com.refinedmods.refinedstorage2.platform.common.util.RedstoneMode;

import net.minecraft.resources.ResourceLocation;

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

    public static final PropertyType<Integer> PRIORITY = integer(createIdentifier("priority"));

    public static final PropertyType<AccessMode> ACCESS_MODE = new PropertyType<>(
        createIdentifier("access_mode"),
        AccessModeSettings::getAccessMode,
        AccessModeSettings::getAccessMode
    );

    public static final PropertyType<SchedulingModeType> SCHEDULING_MODE = new PropertyType<>(
        createIdentifier("scheduling_mode"),
        SchedulingModeType::getId,
        SchedulingModeType::getById
    );

    public static final PropertyType<Boolean> FUZZY_MODE = bool(createIdentifier("fuzzy_mode"));

    public static final PropertyType<DetectorMode> DETECTOR_MODE = new PropertyType<>(
        createIdentifier("detector_mode"),
        DetectorModeSettings::getDetectorMode,
        DetectorModeSettings::getDetectorMode
    );

    public static final PropertyType<Boolean> DESTRUCTOR_PICKUP_ITEMS = bool(createIdentifier("pickup_items"));

    private PropertyTypes() {
    }

    private static PropertyType<Boolean> bool(final ResourceLocation id) {
        return new PropertyType<>(
            id,
            value -> Boolean.TRUE.equals(value) ? 1 : 0,
            value -> value == 1
        );
    }

    private static PropertyType<Integer> integer(final ResourceLocation id) {
        return new PropertyType<>(
            id,
            value -> value,
            value -> value
        );
    }
}
