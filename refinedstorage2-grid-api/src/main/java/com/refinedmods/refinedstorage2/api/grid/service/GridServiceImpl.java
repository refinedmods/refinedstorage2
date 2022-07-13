package com.refinedmods.refinedstorage2.api.grid.service;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.ExtractableStorage;
import com.refinedmods.refinedstorage2.api.storage.InsertableStorage;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;

import java.util.function.Function;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.2")
public class GridServiceImpl<T> implements GridService<T> {
    private final StorageChannel<T> storageChannel;
    private final Actor actor;
    private final Function<T, Long> maxCountProvider;
    private final long singleAmount;

    /**
     * @param storageChannel   the storage channel to act on
     * @param actor            the actor performing the grid interactions
     * @param maxCountProvider provider for the maximum amount of a given resource
     * @param singleAmount     amount that needs to be extracted when using
     *                         {@link GridInsertMode#SINGLE_RESOURCE} or {@link GridExtractMode#SINGLE_RESOURCE}
     */
    public GridServiceImpl(final StorageChannel<T> storageChannel,
                           final Actor actor,
                           final Function<T, Long> maxCountProvider,
                           final long singleAmount) {
        this.storageChannel = storageChannel;
        this.actor = actor;
        this.maxCountProvider = maxCountProvider;
        this.singleAmount = singleAmount;
    }

    @Override
    public void extract(final T resource, final GridExtractMode extractMode, final InsertableStorage<T> destination) {
        final long amount = getExtractableAmount(resource, extractMode);
        if (amount == 0) {
            return;
        }
        long extractedFromSource = storageChannel.extract(resource, amount, Action.SIMULATE, actor);
        if (extractedFromSource == 0) {
            return;
        }
        final long amountInsertedIntoDestination = destination.insert(
            resource,
            extractedFromSource,
            Action.SIMULATE,
            actor
        );
        if (amountInsertedIntoDestination > 0) {
            extractedFromSource = storageChannel.extract(
                resource,
                amountInsertedIntoDestination,
                Action.EXECUTE,
                actor
            );
            destination.insert(resource, extractedFromSource, Action.EXECUTE, actor);
        }
    }

    private long getExtractableAmount(final T resource, final GridExtractMode extractMode) {
        final long extractableAmount = getExtractableAmount(resource);
        return switch (extractMode) {
            case ENTIRE_RESOURCE -> extractableAmount;
            case HALF_RESOURCE -> extractableAmount == 1 ? 1 : extractableAmount / 2;
            case SINGLE_RESOURCE -> Math.min(singleAmount, extractableAmount);
        };
    }

    private long getExtractableAmount(final T resource) {
        final long maxCount = maxCountProvider.apply(resource);
        final long totalSize = storageChannel.get(resource).map(ResourceAmount::getAmount).orElse(0L);
        return Math.min(maxCount, totalSize);
    }

    @Override
    public void insert(final T resource, final GridInsertMode insertMode, final ExtractableStorage<T> source) {
        final long amount = switch (insertMode) {
            case ENTIRE_RESOURCE -> maxCountProvider.apply(resource);
            case SINGLE_RESOURCE -> singleAmount;
        };
        long extractedFromSource = source.extract(resource, amount, Action.SIMULATE, actor);
        if (extractedFromSource == 0) {
            return;
        }
        final long amountInsertedIntoDestination = storageChannel.insert(
            resource,
            extractedFromSource,
            Action.SIMULATE,
            this.actor
        );
        if (amountInsertedIntoDestination > 0) {
            extractedFromSource = source.extract(resource, amountInsertedIntoDestination, Action.EXECUTE, actor);
            if (extractedFromSource > 0) {
                storageChannel.insert(resource, extractedFromSource, Action.EXECUTE, actor);
            }
        }
    }
}
