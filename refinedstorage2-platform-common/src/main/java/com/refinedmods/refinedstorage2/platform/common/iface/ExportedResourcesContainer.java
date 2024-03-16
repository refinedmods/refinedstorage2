package com.refinedmods.refinedstorage2.platform.common.iface;

import com.refinedmods.refinedstorage2.api.network.impl.node.iface.InterfaceExportState;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.FuzzyStorageChannel;
import com.refinedmods.refinedstorage2.platform.api.support.resource.FuzzyModeNormalizer;
import com.refinedmods.refinedstorage2.platform.common.support.FilterWithFuzzyMode;
import com.refinedmods.refinedstorage2.platform.common.support.resource.ResourceContainerImpl;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

public class ExportedResourcesContainer extends ResourceContainerImpl implements InterfaceExportState {
    private final FilterWithFuzzyMode filter;

    ExportedResourcesContainer(final int size, final FilterWithFuzzyMode filter) {
        super(
            size,
            InterfaceBlockEntity::getTransferQuota,
            PlatformApi.INSTANCE.getItemResourceFactory(),
            PlatformApi.INSTANCE.getAlternativeResourceFactories()
        );
        this.filter = filter;
    }

    @Override
    public int getSlots() {
        return size();
    }

    @Override
    public Collection<ResourceKey> expandExportCandidates(final StorageChannel storageChannel,
                                                          final ResourceKey resource) {
        if (!filter.isFuzzyMode()) {
            return Collections.singletonList(resource);
        }
        if (!(storageChannel instanceof FuzzyStorageChannel fuzzyStorageChannel)) {
            return Collections.singletonList(resource);
        }
        return fuzzyStorageChannel
            .getFuzzy(resource)
            .stream()
            .map(ResourceAmount::getResource)
            .collect(Collectors.toSet());
    }

    @Override
    public boolean isExportedResourceValid(final ResourceKey want, final ResourceKey got) {
        if (!filter.isFuzzyMode()) {
            return got.equals(want);
        }
        final ResourceKey normalizedGot = normalize(got);
        final ResourceKey normalizedWant = normalize(want);
        return normalizedGot.equals(normalizedWant);
    }

    private ResourceKey normalize(final ResourceKey resource) {
        if (resource instanceof FuzzyModeNormalizer normalizer) {
            return normalizer.normalize();
        }
        return resource;
    }

    @Nullable
    @Override
    public ResourceKey getRequestedResource(final int slotIndex) {
        return filter.getFilterContainer().getResource(slotIndex);
    }

    @Override
    public long getRequestedAmount(final int slotIndex) {
        return filter.getFilterContainer().getAmount(slotIndex);
    }

    @Nullable
    @Override
    public ResourceKey getExportedResource(final int slotIndex) {
        return getResource(slotIndex);
    }

    @Override
    public long getExportedAmount(final int slotIndex) {
        return getAmount(slotIndex);
    }

    @Override
    public void setExportSlot(final int slotIndex, final ResourceKey resource, final long amount) {
        set(slotIndex, new ResourceAmount(resource, amount));
    }

    @Override
    public void shrinkExportedAmount(final int slotIndex, final long amount) {
        shrink(slotIndex, amount);
    }

    @Override
    public void growExportedAmount(final int slotIndex, final long amount) {
        grow(slotIndex, amount);
    }
}
