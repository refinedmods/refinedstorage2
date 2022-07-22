package com.refinedmods.refinedstorage2.api.grid.service;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.ExtractableStorage;
import com.refinedmods.refinedstorage2.api.storage.InsertableStorage;
import com.refinedmods.refinedstorage2.api.storage.TransferHelper;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;

import java.util.function.Function;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.2")
public class GridServiceImpl<T> implements GridService<T> {
    private final StorageChannel<T> storageChannel;
    private final Actor actor;
    private final Function<T, Long> maxAmountProvider;
    private final long singleAmount;

    /**
     * @param storageChannel    the storage channel to act on
     * @param actor             the actor performing the grid interactions
     * @param maxAmountProvider provider for the maximum amount of a given resource
     * @param singleAmount      amount that needs to be extracted when using
     *                          {@link GridInsertMode#SINGLE_RESOURCE} or {@link GridExtractMode#SINGLE_RESOURCE}
     */
    public GridServiceImpl(final StorageChannel<T> storageChannel,
                           final Actor actor,
                           final Function<T, Long> maxAmountProvider,
                           final long singleAmount) {
        this.storageChannel = storageChannel;
        this.actor = actor;
        this.maxAmountProvider = maxAmountProvider;
        this.singleAmount = singleAmount;
    }

    @Override
    public void extract(final T resource, final GridExtractMode extractMode, final InsertableStorage<T> destination) {
        final long amount = getExtractableAmount(resource, extractMode);
        if (amount == 0) {
            return;
        }
        TransferHelper.transfer(resource, amount, actor, storageChannel, destination, null);
    }

    private long getExtractableAmount(final T resource, final GridExtractMode extractMode) {
        final long extractableAmount = getExtractableAmount(resource);
        return adjustExtractableAmountAccordingToExtractMode(extractMode, extractableAmount);
    }

    private long getExtractableAmount(final T resource) {
        final long totalSize = storageChannel.get(resource).map(ResourceAmount::getAmount).orElse(0L);
        final long maxAmount = maxAmountProvider.apply(resource);
        return Math.min(totalSize, maxAmount);
    }

    private long adjustExtractableAmountAccordingToExtractMode(final GridExtractMode extractMode,
                                                               final long extractableAmount) {
        return switch (extractMode) {
            case ENTIRE_RESOURCE -> extractableAmount;
            case HALF_RESOURCE -> extractableAmount == 1 ? 1 : extractableAmount / 2;
            case SINGLE_RESOURCE -> Math.min(singleAmount, extractableAmount);
        };
    }

    @Override
    public void insert(final T resource, final GridInsertMode insertMode, final ExtractableStorage<T> source) {
        final long amount = switch (insertMode) {
            case ENTIRE_RESOURCE -> maxAmountProvider.apply(resource);
            case SINGLE_RESOURCE -> singleAmount;
        };
        TransferHelper.transfer(resource, amount, actor, source, storageChannel, null);
    }
}
