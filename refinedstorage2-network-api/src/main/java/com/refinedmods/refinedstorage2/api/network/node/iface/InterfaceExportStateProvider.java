package com.refinedmods.refinedstorage2.api.network.node.iface;

import javax.annotation.Nullable;

@FunctionalInterface
public interface InterfaceExportStateProvider<T> {
    @Nullable
    InterfaceExportState<T> getExportState();
}
