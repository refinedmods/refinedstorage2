package com.refinedmods.refinedstorage2.api.network.impl.node.storagetransfer;

@FunctionalInterface
public interface StorageTransferListener {
    void onTransferSuccess(int index);
}
