package com.refinedmods.refinedstorage2.platform.fabric.internal.grid.view;

import com.refinedmods.refinedstorage2.api.grid.view.GridResourceAttributeKey;

import java.util.Map;
import java.util.Set;

public enum GridResourceAttributeKeys implements GridResourceAttributeKey {
    MOD_ID,
    MOD_NAME,
    TAGS,
    TOOLTIP;

    public static final Map<String, Set<GridResourceAttributeKey>> UNARY_OPERATOR_TO_ATTRIBUTE_KEY_MAPPING = Map.of(
            "@", Set.of(GridResourceAttributeKeys.MOD_ID, GridResourceAttributeKeys.MOD_NAME),
            "$", Set.of(GridResourceAttributeKeys.TAGS),
            "#", Set.of(GridResourceAttributeKeys.TOOLTIP)
    );
}
