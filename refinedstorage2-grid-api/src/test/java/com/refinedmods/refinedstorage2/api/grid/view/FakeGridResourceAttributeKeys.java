package com.refinedmods.refinedstorage2.api.grid.view;

import java.util.Map;
import java.util.Set;

public enum FakeGridResourceAttributeKeys implements GridResourceAttributeKey {
    MOD_ID,
    MOD_NAME,
    TAGS;

    public static final Map<String, Set<GridResourceAttributeKey>> UNARY_OPERATOR_TO_ATTRIBUTE_KEY_MAPPING = Map.of(
        "@", Set.of(MOD_ID, MOD_NAME),
        "$", Set.of(TAGS)
    );
}
