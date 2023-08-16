package com.refinedmods.refinedstorage2.platform.common.block.entity.iface;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.impl.node.iface.InterfaceExportState;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.ResourceTemplate;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.resource.FuzzyModeNormalizer;
import com.refinedmods.refinedstorage2.platform.api.resource.ResourceAmountTemplate;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.FuzzyStorageChannel;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;
import com.refinedmods.refinedstorage2.platform.common.block.entity.FilterWithFuzzyMode;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.ResourceContainer;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.ResourceContainerType;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

public class ExportedResourcesContainer extends ResourceContainer implements InterfaceExportState {
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
    public <T> Collection<T> expandExportCandidates(final StorageChannel<T> storageChannel,
                                                    final T resource) {
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
    public <A, B> boolean isExportedResourceValid(
        final ResourceTemplate<A> want,
        final ResourceTemplate<B> got
    ) {
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

    // TODO: introduce itemhandler/fluidhandlers..
    // TODO: fix/add more tests.
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

    @Override
    public <T> long insert(final StorageChannelType<T> storageChannelType,
                           final T resource,
                           final long amount,
                           final Action action) {
        if (!(storageChannelType instanceof PlatformStorageChannelType<T> platformStorageChannelType)) {
            return 0;
        }
        long remainder = amount;
        for (int i = 0; i < size(); ++i) {
            final ResourceAmountTemplate<?> existing = get(i);
            if (existing == null) {
                remainder -= insertIntoEmptySlot(i, resource, action, platformStorageChannelType, remainder);
            } else if (existing.getResource().equals(resource)) {
                remainder -= insertIntoExistingSlot(
                    i,
                    platformStorageChannelType,
                    resource,
                    action,
                    remainder,
                    existing
                );
            }
            if (remainder == 0) {
                break;
            }
        }
        return amount - remainder;
    }

    private <T> long insertIntoEmptySlot(final int slotIndex,
                                         final T resource,
                                         final Action action,
                                         final PlatformStorageChannelType<T> platformStorageChannelType,
                                         final long amount) {
        final long inserted = Math.min(platformStorageChannelType.getInterfaceExportLimit(resource), amount);
        if (action == Action.EXECUTE) {
            set(slotIndex, new ResourceAmountTemplate<>(
                resource,
                inserted,
                platformStorageChannelType
            ));
        }
        return inserted;
    }

    private <T> long insertIntoExistingSlot(final int slotIndex,
                                            final PlatformStorageChannelType<T> storageChannelType,
                                            final T resource,
                                            final Action action,
                                            final long amount,
                                            final ResourceAmountTemplate<?> existing) {
        final long spaceRemaining = storageChannelType.getInterfaceExportLimit(resource) - existing.getAmount();
        final long inserted = Math.min(spaceRemaining, amount);
        if (action == Action.EXECUTE) {
            grow(slotIndex, inserted);
        }
        return inserted;
    }
}
