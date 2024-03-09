package com.refinedmods.refinedstorage2.api.storage.composite;

import com.refinedmods.refinedstorage2.api.storage.AbstractProxyStorage;
import com.refinedmods.refinedstorage2.api.storage.InMemoryStorageImpl;

class ConsumingStorageImpl extends AbstractProxyStorage implements ConsumingStorage {
    ConsumingStorageImpl() {
        super(new InMemoryStorageImpl());
    }
}
