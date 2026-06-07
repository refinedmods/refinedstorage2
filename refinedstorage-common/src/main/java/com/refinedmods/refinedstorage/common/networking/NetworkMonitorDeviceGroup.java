package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.common.api.networking.NetworkMonitorDeviceType;

import java.util.List;
import java.util.UUID;

public record NetworkMonitorDeviceGroup(UUID id, NetworkMonitorDeviceType type, long energyUsage,
                                        List<NetworkMonitorDevice> devices) {
}
