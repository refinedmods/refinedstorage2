package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.api.network.impl.node.monitor.MonitorNodeId;
import com.refinedmods.refinedstorage.api.network.impl.node.monitor.MonitorNodeTypeId;
import com.refinedmods.refinedstorage.common.api.networking.NetworkMonitorDeviceType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jspecify.annotations.Nullable;

public record NetworkMonitorDeviceGroup(UUID id, NetworkMonitorDeviceType type, List<NetworkMonitorDevice> devices) {
    static final StreamCodec<RegistryFriendlyByteBuf, NetworkMonitorDeviceGroup> STREAM_CODEC = StreamCodec.composite(
        UUIDUtil.STREAM_CODEC, NetworkMonitorDeviceGroup::id,
        NetworkMonitorDeviceType.STREAM_CODEC, NetworkMonitorDeviceGroup::type,
        ByteBufCodecs.collection(ArrayList::new, NetworkMonitorDevice.STREAM_CODEC),
        NetworkMonitorDeviceGroup::devices,
        NetworkMonitorDeviceGroup::new
    );

    static NetworkMonitorDeviceGroup create(final MonitorNodeTypeId id, final NetworkMonitorDeviceType type) {
        return new NetworkMonitorDeviceGroup(id.id(), type, new ArrayList<>());
    }

    @Nullable
    NetworkMonitorDevice removeDeviceById(final MonitorNodeId deviceId) {
        final NetworkMonitorDevice device = findDeviceById(deviceId);
        if (device != null) {
            devices.remove(device);
        }
        return device;
    }

    @Nullable
    private NetworkMonitorDevice findDeviceById(final MonitorNodeId deviceId) {
        return devices.stream()
            .filter(device -> device.id().equals(deviceId.id()))
            .findFirst()
            .orElse(null);
    }

    boolean hasDevice(final MonitorNodeId deviceId) {
        return findDeviceById(deviceId) != null;
    }

    long totalEnergyUsage() {
        return devices.stream()
            .mapToLong(NetworkMonitorDevice::energyUsage)
            .sum();
    }

    @Override
    public boolean equals(@Nullable final Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final NetworkMonitorDeviceGroup that = (NetworkMonitorDeviceGroup) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
