package com.refinedmods.refinedstorage2.api.grid.service;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.stack.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.Source;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;

import java.util.Optional;

public class GridServiceImpl<T> implements GridService<T> {
    private final StorageChannel<T> storageChannel;
    private final Source source;

    public GridServiceImpl(StorageChannel<T> storageChannel, Source source) {
        this.storageChannel = storageChannel;
        this.source = source;
    }

    @Override
    public Optional<ResourceAmount<T>> insert(ResourceAmount<T> resourceAmount, GridInsertMode insertMode) {
        return switch (insertMode) {
            case ENTIRE_RESOURCE -> insertEntireResource(resourceAmount);
            case SINGLE_RESOURCE -> insertSingleResource(resourceAmount);
        };
    }

    private Optional<ResourceAmount<T>> insertSingleResource(ResourceAmount<T> resourceAmount) {
        long remainder = storageChannel.insert(resourceAmount.getResource(), 1, Action.SIMULATE);
        boolean insertedSomething = remainder == 0;
        if (insertedSomething) {
            storageChannel.insert(resourceAmount.getResource(), 1, source);
            if (resourceAmount.getAmount() - 1 == 0) {
                return Optional.empty();
            }
            return Optional.of(new ResourceAmount<>(resourceAmount.getResource(), resourceAmount.getAmount() - 1));
        } else {
            return Optional.of(resourceAmount);
        }
    }

    private Optional<ResourceAmount<T>> insertEntireResource(ResourceAmount<T> resourceAmount) {
        long remainder = storageChannel.insert(resourceAmount.getResource(), resourceAmount.getAmount(), Action.SIMULATE);
        boolean insertedSomething = remainder != resourceAmount.getAmount();
        if (insertedSomething) {
            remainder = storageChannel.insert(resourceAmount.getResource(), resourceAmount.getAmount(), source);
            if (remainder > 0) {
                return Optional.of(new ResourceAmount<>(resourceAmount.getResource(), remainder));
            }
            return Optional.empty();
        } else {
            return Optional.of(resourceAmount);
        }
    }
}
