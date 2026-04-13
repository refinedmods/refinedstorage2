package com.refinedmods.refinedstorage.api.network.node;

import com.refinedmods.refinedstorage.api.network.Network;

import org.apiguardian.api.API;
import org.jspecify.annotations.Nullable;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public interface NetworkNode {
    @Nullable
    Network getNetwork();

    void setNetwork(@Nullable Network network);
}
