package com.refinedmods.refinedstorage.neoforge.storage;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.Iterator;
import java.util.Optional;

import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.resource.Resource;

public interface ResourceHandlerProvider<T extends Resource> {
    Optional<ResourceHandler<T>> resolve();

    Iterator<ResourceKey> iterator();

    Iterator<ResourceAmount> amountIterator();
}
