package com.refinedmods.refinedstorage2.api.storage.channel;

import com.refinedmods.refinedstorage2.api.stack.fluid.Rs2FluidStack;
import com.refinedmods.refinedstorage2.api.stack.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.api.stack.list.StackListImpl;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.composite.CompositeStorage;

import java.util.Collections;
import java.util.List;

public final class StorageChannelTypes {
    private StorageChannelTypes() {
    }

    public static final StorageChannelType<Rs2ItemStack> ITEM = new StorageChannelType<>() {
        @Override
        public StorageChannel<Rs2ItemStack> create() {
            return new StorageChannelImpl<>(
                    StackListImpl::new,
                    new StorageTracker<>(System::currentTimeMillis),
                    new CompositeStorage<>(Collections.emptyList(), new StackListImpl<>())
            );
        }

        @Override
        public CompositeStorage<Rs2ItemStack> createEmptyCompositeStorage() {
            return new CompositeStorage<>(Collections.emptyList(), new StackListImpl<>());
        }

        @Override
        public CompositeStorage<Rs2ItemStack> createCompositeStorage(List<Storage<Rs2ItemStack>> sources) {
            return new CompositeStorage<>(sources, new StackListImpl<>());
        }

        @Override
        public String toString() {
            return "item";
        }
    };

    public static final StorageChannelType<Rs2FluidStack> FLUID = new StorageChannelType<>() {
        @Override
        public StorageChannel<Rs2FluidStack> create() {
            return new StorageChannelImpl<>(
                    StackListImpl::new,
                    new StorageTracker<>(System::currentTimeMillis),
                    new CompositeStorage<>(Collections.emptyList(), new StackListImpl<>())
            );
        }

        @Override
        public CompositeStorage<Rs2FluidStack> createEmptyCompositeStorage() {
            return new CompositeStorage<>(Collections.emptyList(), new StackListImpl<>());
        }

        @Override
        public CompositeStorage<Rs2FluidStack> createCompositeStorage(List<Storage<Rs2FluidStack>> sources) {
            return new CompositeStorage<>(sources, new StackListImpl<>());
        }

        @Override
        public String toString() {
            return "fluid";
        }
    };
}
