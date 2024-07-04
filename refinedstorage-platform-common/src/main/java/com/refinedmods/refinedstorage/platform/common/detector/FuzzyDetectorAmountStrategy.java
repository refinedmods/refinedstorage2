package com.refinedmods.refinedstorage.platform.common.detector;

import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.impl.node.detector.AbstractDetectorAmountStrategy;
import com.refinedmods.refinedstorage.api.network.impl.node.detector.DetectorAmountStrategy;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage.platform.api.storage.channel.FuzzyStorageChannel;

class FuzzyDetectorAmountStrategy extends AbstractDetectorAmountStrategy {
    private final DetectorAmountStrategy fallback;

    FuzzyDetectorAmountStrategy(final DetectorAmountStrategy fallback) {
        this.fallback = fallback;
    }

    @Override
    public long getAmount(final Network network, final ResourceKey configuredResource) {
        final StorageChannel storageChannel = getStorageChannel(network);
        if (!(storageChannel instanceof FuzzyStorageChannel fuzzyStorageChannel)) {
            return fallback.getAmount(network, configuredResource);
        }
        return fuzzyStorageChannel.getFuzzy(configuredResource)
            .stream()
            .mapToLong(ResourceAmount::getAmount)
            .sum();
    }
}
