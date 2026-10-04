package com.refinedmods.refinedstorage.common.support.network;

import com.refinedmods.refinedstorage.api.network.node.NetworkNodeType;

public enum PlatformNetworkNodeTypes implements NetworkNodeType {
    STORAGE_BLOCK,
    DISK_DRIVE,
    DISK_INTERFACE,
    AUTOCRAFTER_MANAGER,
    WIRELESS_TRANSMITTER,
    NETWORK_RECEIVER,
    NETWORK_TRANSMITTER,
    CABLE,
    DESTRUCTOR,
    CONSTRUCTOR,
    AUTOCRAFTING_MONITOR,
    STORAGE_MONITOR
}
