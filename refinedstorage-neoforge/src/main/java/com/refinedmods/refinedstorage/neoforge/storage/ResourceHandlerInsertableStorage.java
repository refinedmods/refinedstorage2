package com.refinedmods.refinedstorage.neoforge.storage;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.InsertableStorage;

import java.util.Optional;
import java.util.function.Function;

import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public class ResourceHandlerInsertableStorage<T extends Resource> implements InsertableStorage {
    private final ResourceHandlerProvider<T> provider;
    private final Function<ResourceKey, Optional<T>> mapper;

    public ResourceHandlerInsertableStorage(final ResourceHandlerProvider<T> provider,
                                            final Function<ResourceKey, Optional<T>> mapper) {
        this.provider = provider;
        this.mapper = mapper;
    }

    public long getAmount(final ResourceKey resource) {
        return mapper.apply(resource).flatMap(platformResource -> provider.resolve()
                .map(handler -> getAmount(platformResource, handler)))
            .orElse(0L);
    }

    private long getAmount(final T platformResource, final ResourceHandler<T> handler) {
        try (Transaction tx = Transaction.openRoot()) {
            return handler.extract(platformResource, Integer.MAX_VALUE, tx);
        }
    }

    @Override
    public long insert(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
        return mapper.apply(resource).flatMap(platformResource -> provider.resolve()
                .map(handler -> insert(amount, action, handler, platformResource)))
            .orElse(0L);
    }

    private long insert(final long amount, final Action action, final ResourceHandler<T> handler, final T resource) {
        try (Transaction tx = Transaction.openRoot()) {
            final int inserted = handler.insert(resource, (int) amount, tx);
            if (action == Action.EXECUTE) {
                tx.commit();
            }
            return inserted;
        }
    }
}
