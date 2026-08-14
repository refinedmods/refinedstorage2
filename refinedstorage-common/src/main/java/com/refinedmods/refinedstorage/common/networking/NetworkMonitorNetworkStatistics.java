package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.storage.StorageType;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record NetworkMonitorNetworkStatistics(long energyUsage, long energyStored,
                                              long energyCapacity, int amountOfDevices,
                                              List<StorageStatistics> storageStatistics) {
    public static final StreamCodec<RegistryFriendlyByteBuf, NetworkMonitorNetworkStatistics> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.LONG, NetworkMonitorNetworkStatistics::energyUsage,
            ByteBufCodecs.LONG, NetworkMonitorNetworkStatistics::energyStored,
            ByteBufCodecs.LONG, NetworkMonitorNetworkStatistics::energyCapacity,
            ByteBufCodecs.INT, NetworkMonitorNetworkStatistics::amountOfDevices,
            ByteBufCodecs.collection(ArrayList::new,
                StorageStatistics.STREAM_CODEC), NetworkMonitorNetworkStatistics::storageStatistics,
            NetworkMonitorNetworkStatistics::new
        );

    double energyPct() {
        if (energyCapacity == 0) {
            return 0;
        }
        return (double) energyStored / (double) energyCapacity;
    }

    long stored(final StorageType storageType) {
        for (final StorageStatistics stat : storageStatistics) {
            if (stat.type().equals(storageType)) {
                return stat.stored();
            }
        }
        return 0;
    }

    long capacity(final StorageType storageType) {
        for (final StorageStatistics stat : storageStatistics) {
            if (stat.type().equals(storageType)) {
                return stat.capacity();
            }
        }
        return 0;
    }

    double storageTypePct(final StorageType storageType) {
        for (final StorageStatistics stat : storageStatistics) {
            if (stat.type().equals(storageType)) {
                if (stat.capacity() == 0) {
                    return 0;
                }
                return (double) stat.stored() / (double) stat.capacity();
            }
        }
        return 0;
    }

    public record StorageStatistics(StorageType type, long stored, long capacity) {
        public static final StreamCodec<RegistryFriendlyByteBuf, StorageStatistics> STREAM_CODEC =
            StreamCodec.composite(
                RefinedStorageApi.INSTANCE.getStorageTypeRegistry().streamCodec(), StorageStatistics::type,
                ByteBufCodecs.LONG, StorageStatistics::stored,
                ByteBufCodecs.LONG, StorageStatistics::capacity,
                StorageStatistics::new
            );
    }
}
