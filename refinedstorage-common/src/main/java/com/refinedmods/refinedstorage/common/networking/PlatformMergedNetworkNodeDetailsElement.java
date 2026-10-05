package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.api.network.node.MergedNetworkNodeDetailsElement;

public interface PlatformMergedNetworkNodeDetailsElement<T> extends MergedNetworkNodeDetailsElement<T> {
    void build(T state, PlatformNetworkNodeDetailsBuilder builder);
}
