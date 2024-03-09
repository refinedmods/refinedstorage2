package com.refinedmods.refinedstorage2.api.network.impl.node.iface.externalstorage;

import com.refinedmods.refinedstorage2.api.network.impl.node.iface.InterfaceNetworkNode;
import com.refinedmods.refinedstorage2.api.storage.external.ExternalStorageProvider;

import javax.annotation.Nullable;

public interface InterfaceExternalStorageProvider extends ExternalStorageProvider {
    @Nullable
    InterfaceNetworkNode getInterface();
}
