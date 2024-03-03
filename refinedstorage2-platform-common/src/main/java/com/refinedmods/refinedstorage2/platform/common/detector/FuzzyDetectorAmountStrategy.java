package com.refinedmods.refinedstorage2.platform.common.detector;

import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.impl.node.detector.AbstractDetectorAmountStrategy;
import com.refinedmods.refinedstorage2.api.network.impl.node.detector.DetectorAmountStrategy;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.ResourceTemplate;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.FuzzyStorageChannel;

class FuzzyDetectorAmountStrategy extends AbstractDetectorAmountStrategy {
    private final DetectorAmountStrategy fallback;

    FuzzyDetectorAmountStrategy(final DetectorAmountStrategy fallback) {
        this.fallback = fallback;
    }

    @Override
    public long getAmount(final Network network, final ResourceTemplate template) {
        final StorageChannel storageChannel = getStorageChannel(network, template);
        if (!(storageChannel instanceof FuzzyStorageChannel fuzzyStorageChannel)) {
            return fallback.getAmount(network, template);
        }
        return fuzzyStorageChannel.getFuzzy(template.resource())
            .stream()
            .mapToLong(ResourceAmount::getAmount)
            .sum();
    }
}
