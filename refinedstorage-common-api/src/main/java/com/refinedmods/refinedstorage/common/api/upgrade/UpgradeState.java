package com.refinedmods.refinedstorage.common.api.upgrade;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.10")
public interface UpgradeState {
    default boolean has(final UpgradeItem upgradeItem) {
        return getAmount(upgradeItem) > 0;
    }

    long getRegulatedAmount(ResourceKey resource);

    int getAmount(UpgradeItem upgradeItem);
}
