package com.refinedmods.refinedstorage.platform.api.grid;

import com.refinedmods.refinedstorage.api.grid.view.GridResourceAttributeKey;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.6")
public enum GridResourceAttributeKeys implements GridResourceAttributeKey {
    MOD_ID,
    MOD_NAME,
    TAGS,
    TOOLTIP
}
