package com.refinedmods.refinedstorage2.platform.fabric.internal.storage.disk;

import com.refinedmods.refinedstorage2.api.stack.list.StackListImpl;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelImpl;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageTracker;
import com.refinedmods.refinedstorage2.api.storage.composite.CompositeStorage;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.ItemResource;

import java.util.Collections;
import java.util.List;

public final class StorageChannelTypes {
    public static final StorageChannelType<ItemResource> ITEM = new StorageChannelType<>() {
        @Override
        public StorageChannel<ItemResource> create() {
            return new StorageChannelImpl<>(
                    StackListImpl::new,
                    new StorageTracker<>(System::currentTimeMillis),
                    new CompositeStorage<>(Collections.emptyList(), new StackListImpl<>())
            );
        }

        @Override
        public CompositeStorage<ItemResource> createEmptyCompositeStorage() {
            return new CompositeStorage<>(Collections.emptyList(), new StackListImpl<>());
        }

        @Override
        public CompositeStorage<ItemResource> createCompositeStorage(List<Storage<ItemResource>> sources) {
            return new CompositeStorage<>(sources, new StackListImpl<>());
        }
    };

    public static final StorageChannelType<FluidResource> FLUID = new StorageChannelType<>() {
        @Override
        public StorageChannel<FluidResource> create() {
            return new StorageChannelImpl<>(
                    StackListImpl::new,
                    new StorageTracker<>(System::currentTimeMillis),
                    new CompositeStorage<>(Collections.emptyList(), new StackListImpl<>())
            );
        }

        @Override
        public CompositeStorage<FluidResource> createEmptyCompositeStorage() {
            return new CompositeStorage<>(Collections.emptyList(), new StackListImpl<>());
        }

        @Override
        public CompositeStorage<FluidResource> createCompositeStorage(List<Storage<FluidResource>> sources) {
            return new CompositeStorage<>(sources, new StackListImpl<>());
        }
    };

    private StorageChannelTypes() {
    }
}
