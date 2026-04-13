package com.refinedmods.refinedstorage.common.api.support.network.item;

import com.refinedmods.refinedstorage.api.network.Network;

import org.apiguardian.api.API;
import org.jspecify.annotations.Nullable;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.6")
@FunctionalInterface
public interface NetworkItemTargetBlockEntity {
    @Nullable
    Network getNetworkForItem();
}
