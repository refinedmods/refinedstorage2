package com.refinedmods.refinedstorage.neoforge.support.inventory;

import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class InsertExtractResourceHandler<T extends Resource> implements ResourceHandler<T> {
    private final ResourceHandler<T> insertHandler;
    private final ResourceHandler<T> extractHandler;

    public InsertExtractResourceHandler(final ResourceHandler<T> insertHandler,
                                        final ResourceHandler<T> extractHandler) {
        this.insertHandler = insertHandler;
        this.extractHandler = extractHandler;
    }

    @Override
    public int size() {
        return insertHandler.size() + extractHandler.size();
    }

    @Override
    public T getResource(final int index) {
        return index < insertHandler.size()
            ? insertHandler.getResource(index)
            : extractHandler.getResource(index - insertHandler.size());
    }

    @Override
    public long getAmountAsLong(final int index) {
        return index < insertHandler.size()
            ? insertHandler.getAmountAsLong(index)
            : extractHandler.getAmountAsLong(index - insertHandler.size());
    }

    @Override
    public long getCapacityAsLong(final int index, final T resource) {
        return index < insertHandler.size()
            ? insertHandler.getCapacityAsLong(index, resource)
            : extractHandler.getCapacityAsLong(index - insertHandler.size(), resource);
    }

    @Override
    public boolean isValid(final int index, final T resource) {
        return index < insertHandler.size()
            ? insertHandler.isValid(index, resource)
            : extractHandler.isValid(index - insertHandler.size(), resource);
    }

    @Override
    public int insert(final int index, final T resource, final int amount, final TransactionContext transaction) {
        return index < insertHandler.size()
            ? insertHandler.insert(index, resource, amount, transaction)
            : 0;
    }

    @Override
    public int extract(final int index, final T resource, final int amount, final TransactionContext transaction) {
        return index >= insertHandler.size()
            ? extractHandler.extract(index - insertHandler.size(), resource, amount, transaction)
            : 0;
    }
}
