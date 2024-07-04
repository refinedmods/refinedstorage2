package com.refinedmods.refinedstorage.api.network.impl.node.storagetransfer;

@FunctionalInterface
public interface StorageTransferListener {
    void onTransferSuccess(int index);
}
