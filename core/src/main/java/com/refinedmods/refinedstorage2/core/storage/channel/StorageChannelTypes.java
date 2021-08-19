package com.refinedmods.refinedstorage2.core.storage.channel;

import com.refinedmods.refinedstorage2.core.list.item.StackListImpl;
import com.refinedmods.refinedstorage2.core.stack.fluid.Rs2FluidStack;
import com.refinedmods.refinedstorage2.core.stack.fluid.Rs2FluidStackIdentifier;
import com.refinedmods.refinedstorage2.core.stack.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.core.stack.item.Rs2ItemStackIdentifier;
import com.refinedmods.refinedstorage2.core.storage.Storage;
import com.refinedmods.refinedstorage2.core.storage.composite.CompositeStorage;

import java.util.List;

public final class StorageChannelTypes {
    private StorageChannelTypes() {
    }

    public static final StorageChannelType<Rs2ItemStack> ITEM = new StorageChannelType<>() {
        @Override
        public StorageChannel<Rs2ItemStack> create() {
            return new StorageChannelImpl<>(
                    StackListImpl::createItemStackList,
                    new StorageTracker<>(Rs2ItemStackIdentifier::new, System::currentTimeMillis),
                    CompositeStorage.emptyItemStackStorage()
            );
        }

        @Override
        public CompositeStorage<Rs2ItemStack> createEmptyCompositeStorage() {
            return CompositeStorage.emptyItemStackStorage();
        }

        @Override
        public CompositeStorage<Rs2ItemStack> createCompositeStorage(List<Storage<Rs2ItemStack>> sources) {
            return new CompositeStorage<>(sources, StackListImpl.createItemStackList());
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
                    StackListImpl::createFluidStackList,
                    new StorageTracker<>(Rs2FluidStackIdentifier::new, System::currentTimeMillis),
                    CompositeStorage.emptyFluidStackStorage()
            );
        }

        @Override
        public CompositeStorage<Rs2FluidStack> createEmptyCompositeStorage() {
            return CompositeStorage.emptyFluidStackStorage();
        }

        @Override
        public CompositeStorage<Rs2FluidStack> createCompositeStorage(List<Storage<Rs2FluidStack>> sources) {
            return new CompositeStorage<>(sources, StackListImpl.createFluidStackList());
        }

        @Override
        public String toString() {
            return "fluid";
        }
    };
}
