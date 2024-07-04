package com.refinedmods.refinedstorage.platform.common.storage.diskinterface;

import com.refinedmods.refinedstorage.api.network.impl.node.storagetransfer.StorageTransferMode;

class TransferModeSettings {
    private static final int INSERT_INTO_NETWORK = 0;
    private static final int EXTRACT_FROM_NETWORK = 1;

    private TransferModeSettings() {
    }

    static StorageTransferMode getTransferMode(final int transferMode) {
        return switch (transferMode) {
            case INSERT_INTO_NETWORK -> StorageTransferMode.INSERT_INTO_NETWORK;
            case EXTRACT_FROM_NETWORK -> StorageTransferMode.EXTRACT_FROM_NETWORK;
            default -> StorageTransferMode.INSERT_INTO_NETWORK;
        };
    }

    static int getTransferMode(final StorageTransferMode transferMode) {
        return switch (transferMode) {
            case INSERT_INTO_NETWORK -> INSERT_INTO_NETWORK;
            case EXTRACT_FROM_NETWORK -> EXTRACT_FROM_NETWORK;
        };
    }
}
