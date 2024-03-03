package com.refinedmods.refinedstorage2.platform.common.support.containermenu;

import com.refinedmods.refinedstorage2.api.resource.filter.FilterMode;
import com.refinedmods.refinedstorage2.platform.common.support.FilterModeSettings;
import com.refinedmods.refinedstorage2.platform.common.support.RedstoneMode;
import com.refinedmods.refinedstorage2.platform.common.support.RedstoneModeSettings;
import com.refinedmods.refinedstorage2.platform.common.support.SchedulingModeType;

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

    public static final PropertyType<SchedulingModeType> SCHEDULING_MODE = new PropertyType<>(
        createIdentifier("scheduling_mode"),
        SchedulingModeType::getId,
        SchedulingModeType::getById
    );

    public static final PropertyType<Boolean> FUZZY_MODE = createBooleanProperty(createIdentifier("fuzzy_mode"));

    private PropertyTypes() {
    }

    public static PropertyType<Boolean> createBooleanProperty(final ResourceLocation id) {
        return new PropertyType<>(
            id,
            value -> Boolean.TRUE.equals(value) ? 1 : 0,
            value -> value == 1
        );
    }

    public static PropertyType<Integer> createIntegerProperty(final ResourceLocation id) {
        return new PropertyType<>(
            id,
            value -> value,
            value -> value
        );
    }
}
