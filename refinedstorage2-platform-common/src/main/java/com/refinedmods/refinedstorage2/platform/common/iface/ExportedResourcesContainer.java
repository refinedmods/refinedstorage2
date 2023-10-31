package com.refinedmods.refinedstorage2.platform.common.iface;

import com.refinedmods.refinedstorage2.api.network.impl.node.iface.InterfaceExportState;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.ResourceTemplate;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.FuzzyStorageChannel;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;
import com.refinedmods.refinedstorage2.platform.api.support.resource.FuzzyModeNormalizer;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ResourceAmountTemplate;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ResourceContainerType;
import com.refinedmods.refinedstorage2.platform.common.support.FilterWithFuzzyMode;
import com.refinedmods.refinedstorage2.platform.common.support.resource.ResourceContainerImpl;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

public class ExportedResourcesContainer extends ResourceContainerImpl implements InterfaceExportState {
    private final FilterWithFuzzyMode filter;

    public ExportedResourcesContainer(final int size, final FilterWithFuzzyMode filter) {
        super(
            size,
            ResourceContainerType.CONTAINER,
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
    public <T> Collection<T> expandExportCandidates(final StorageChannel<T> storageChannel, final T resource) {
        if (!filter.isFuzzyMode()) {
            return Collections.singletonList(resource);
        }
        if (!(storageChannel instanceof FuzzyStorageChannel<T> fuzzyStorageChannel)) {
            return Collections.singletonList(resource);
        }
        return fuzzyStorageChannel
            .getFuzzy(resource)
            .stream()
            .map(ResourceAmount::getResource)
            .collect(Collectors.toSet());
    }

    @Override
    public <A, B> boolean isExportedResourceValid(final ResourceTemplate<A> want, final ResourceTemplate<B> got) {
        if (!filter.isFuzzyMode()) {
            return got.equals(want);
        }
        final B normalizedGot = normalize(got.resource());
        final A normalizedWant = normalize(want.resource());
        return normalizedGot.equals(normalizedWant);
    }

    @SuppressWarnings("unchecked")
    private <T> T normalize(final T value) {
        if (value instanceof FuzzyModeNormalizer<?> normalizer) {
            return (T) normalizer.normalize();
        }
        return value;
    }

    @Nullable
    @Override
    public ResourceTemplate<?> getRequestedResource(final int slotIndex) {
        final ResourceAmountTemplate<?> resourceAmount = filter.getFilterContainer().get(slotIndex);
        if (resourceAmount == null) {
            return null;
        }
        return resourceAmount.getResourceTemplate();
    }

    @Override
    public long getRequestedAmount(final int slotIndex) {
        final ResourceAmountTemplate<?> resourceAmount = filter.getFilterContainer().get(slotIndex);
        if (resourceAmount == null) {
            return 0;
        }
        return resourceAmount.getAmount();
    }

    @Nullable
    @Override
    public ResourceTemplate<?> getExportedResource(final int slotIndex) {
        final ResourceAmountTemplate<?> resourceAmount = get(slotIndex);
        if (resourceAmount == null) {
            return null;
        }
        return resourceAmount.getResourceTemplate();
    }

    @Override
    public long getExportedAmount(final int slotIndex) {
        return getAmount(slotIndex);
    }

    @Override
    public <T> void setExportSlot(final int slotIndex, final ResourceTemplate<T> resource, final long amount) {
        set(slotIndex, new ResourceAmountTemplate<>(
            resource.resource(),
            amount,
            (PlatformStorageChannelType<T>) resource.storageChannelType()
        ));
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
