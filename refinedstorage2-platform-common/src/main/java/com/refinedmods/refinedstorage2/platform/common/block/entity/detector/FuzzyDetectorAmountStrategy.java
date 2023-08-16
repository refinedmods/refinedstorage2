package com.refinedmods.refinedstorage2.platform.common.block.entity.detector;

import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.impl.node.detector.AbstractDetectorAmountStrategy;
import com.refinedmods.refinedstorage2.api.network.impl.node.detector.DetectorAmountStrategy;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.ResourceTemplate;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.FuzzyStorageChannel;

public class FuzzyDetectorAmountStrategy extends AbstractDetectorAmountStrategy {
    private final DetectorAmountStrategy fallback;

    public FuzzyDetectorAmountStrategy(final DetectorAmountStrategy fallback) {
        this.fallback = fallback;
    }

    @Override
    public <T> long getAmount(final Network network, final ResourceTemplate<T> template) {
        final StorageChannel<T> storageChannel = getStorageChannel(network, template);
        if (!(storageChannel instanceof FuzzyStorageChannel<T> fuzzyStorageChannel)) {
            return fallback.getAmount(network, template);
        }
        return fuzzyStorageChannel.getFuzzy(template.resource())
            .stream()
            .mapToLong(ResourceAmount::getAmount)
            .sum();
    }
}
