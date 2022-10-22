package com.refinedmods.refinedstorage2.api.storage.composite;

import com.refinedmods.refinedstorage2.api.storage.AbstractProxyStorage;
import com.refinedmods.refinedstorage2.api.storage.InMemoryStorageImpl;

public class ConsumingStorageImpl<T> extends AbstractProxyStorage<T> implements ConsumingStorage<T> {
    public ConsumingStorageImpl() {
        super(new InMemoryStorageImpl<>());
    }
}
